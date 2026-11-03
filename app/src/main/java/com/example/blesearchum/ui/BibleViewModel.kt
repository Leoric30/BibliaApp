package com.example.blesearchum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BibleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BibleUiState())
    val uiState: StateFlow<BibleUiState> = _uiState.asStateFlow()

    init {
        fetchBooks()
    }

    fun fetchBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val bookList = RetrofitInstance.api.getBooks()
                _uiState.update {
                    it.copy(books = bookList, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar libros: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun onBookSelected(book: BibleBook) {
        val chaptersList = (1..book.chapters).toList()
        _uiState.update {
            it.copy(
                selectedBook = book,
                availableChapters = chaptersList,
                selectedChapter = null,
                selectedVerse = null,
                availableVerses = emptyList(),
                currentVerseContent = null,
                errorMessage = null
            )
        }
    }

    fun onChapterSelected(chapter: Int) {
        val currentBook = _uiState.value.selectedBook ?: return
        _uiState.update {
            it.copy(
                selectedChapter = chapter,
                availableVerses = emptyList(),
                selectedVerse = null,
                currentVerseContent = null,
                isLoading = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            // Se consulta el capítulo completo para conocer el número real de versículos.
            // Si falla, se usa un máximo genérico de 150 (como en la guía original).
            val versesList = try {
                val count = RetrofitInstance.api.getChapter(currentBook.bookId, chapter).size
                if (count > 0) (1..count).toList() else (1..MAX_VERSES).toList()
            } catch (e: Exception) {
                (1..MAX_VERSES).toList()
            }
            _uiState.update {
                // Evita aplicar resultados de un capítulo que ya no está seleccionado.
                if (it.selectedBook == currentBook && it.selectedChapter == chapter) {
                    it.copy(availableVerses = versesList, isLoading = false)
                } else it
            }
        }
    }

    fun onVerseSelected(verse: Int) {
        val currentBook = _uiState.value.selectedBook ?: return
        val currentChapter = _uiState.value.selectedChapter ?: return

        _uiState.update { it.copy(selectedVerse = verse, isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val result = RetrofitInstance.api.getVerse(currentBook.bookId, currentChapter, verse)
                _uiState.update {
                    it.copy(
                        currentVerseContent = result.copy(text = cleanText(result.text)),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentVerseContent = null,
                        errorMessage = "No se pudo obtener el versículo solicitado."
                    )
                }
            }
        }
    }

    fun clearSelection() {
        _uiState.update {
            BibleUiState(books = it.books)
        }
        if (_uiState.value.books.isEmpty()) fetchBooks()
    }

    /** Elimina etiquetas HTML/Strong que la API puede incluir y normaliza espacios. */
    private fun cleanText(raw: String): String =
        raw.replace(Regex("<S>.*?</S>"), "")
            .replace(Regex("<[^>]+>"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

    private companion object {
        const val MAX_VERSES = 150
    }
}
