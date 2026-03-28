package com.muhwezi.networkbridge.ui.template

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhwezi.networkbridge.data.model.Template
import com.muhwezi.networkbridge.data.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateUiState())
    val uiState: StateFlow<TemplateUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = templateRepository.getTemplates()
            
            if (result.isSuccess) {
                val templates = result.getOrDefault(emptyList())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    templates = templates,
                    loginTemplates = templates.filter { it.templateType == "login" },
                    paymentTemplates = templates.filter { it.templateType == "payment" }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load templates"
                )
            }
        }
    }

    fun refresh() {
        loadTemplates()
    }
}

data class TemplateUiState(
    val templates: List<Template> = emptyList(),
    val loginTemplates: List<Template> = emptyList(),
    val paymentTemplates: List<Template> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
