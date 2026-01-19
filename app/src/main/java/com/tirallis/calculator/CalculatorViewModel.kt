package com.tirallis.calculator

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mariuszgromada.math.mxparser.Expression

class CalculatorViewModel : ViewModel() {

    private val _state: MutableStateFlow<CalculatorState> = MutableStateFlow(
        CalculatorState.Initial
    )
    val state = _state.asStateFlow()

    private var currentExpression = ""

    fun processCommand(command: CalculatorCommand) {
        Log.d("Calculator", "$command was clicked")
        when (command) {
            CalculatorCommand.Clear -> {
                currentExpression = ""
                _state.value = CalculatorState.Initial
            }


            CalculatorCommand.Evaluate -> {
                val result = evaluate()
                if (result != null) {
                    currentExpression = result
                    _state.value = CalculatorState.Success(result)
                } else {
                    _state.value = CalculatorState.Error(currentExpression)
                }
            }

            is CalculatorCommand.Input -> {
                val symbol = if (command.symbol != Symbol.PARENTHESIS) {
                    command.symbol.value
                } else {
                    getCorrectParenthesis()
                }
                currentExpression += symbol
                _state.value = CalculatorState.Input(
                    expression = currentExpression,
                    result = evaluate()?: ""
                )
            }
        }
    }

    private fun evaluate(): String? {
        return Expression(currentExpression
            .replace('x', '*')
            .replace(',', '.'))
            .calculate()
            .takeIf { it.isFinite() }?.toString()
    }

    private fun getCorrectParenthesis(): String {
        val openCount = currentExpression.count { it == '(' }
        val closeCount = currentExpression.count { it == ')' }
        return when {
            currentExpression.isEmpty() -> "("
            currentExpression.last().let { ch ->
                !ch.isDigit() && ch != ')' && ch != 'π'
            } -> "("

            openCount > closeCount -> ")"
            else -> "("

        }
    }
}

sealed interface CalculatorCommand {

    data object Clear : CalculatorCommand
    data object Evaluate : CalculatorCommand
    data class Input(val symbol: Symbol) : CalculatorCommand

}

enum class Symbol(val value: String) {
    DIGIT_0("0"),
    DIGIT_1("1"),
    DIGIT_2("2"),
    DIGIT_3("3"),
    DIGIT_4("4"),
    DIGIT_5("5"),
    DIGIT_6("6"),
    DIGIT_7("7"),
    DIGIT_8("8"),
    DIGIT_9("9"),
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("x"),
    DIVIDE("÷"),
    PERCENT("%"),
    POWER("^"),
    FACTORIAL("!"),
    SQRT("√"),
    PI("π"),
    DOT(","),
    PARENTHESIS("()")
}

sealed interface CalculatorState {

    data object Initial : CalculatorState
    data class Input(
        val expression: String,
        val result: String
    ) : CalculatorState

    data class Success(
        val result: String
    ) : CalculatorState

    data class Error(
        val expression: String
    ) : CalculatorState
}