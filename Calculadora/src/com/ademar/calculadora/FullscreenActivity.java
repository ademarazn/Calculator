package com.ademar.calculadora;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Full-screen activity, ou seja, que exibe em tela cheia.
 * 
 * @author Ademar Zório Neto
 * @since Classe criada em 26/08/2017
 */
public class FullscreenActivity extends Activity {

	TextToSpeech speech;
	EditText visor, instRes;
	Button limpar;

	// Método que será chamado ao executar o aplicativo
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);

		speech = new TextToSpeech(FullscreenActivity.this,
				new TextToSpeech.OnInitListener() {

					@Override
					public void onInit(int status) {
						if (status == TextToSpeech.SUCCESS) {
							speech.setLanguage(Locale.getDefault());
						}
					}
				});

		visor = (EditText) findViewById(R.id.number);
		instRes = (EditText) findViewById(R.id.instRes);
		limpar = (Button) findViewById(R.id.btnLimpar);
		limpar.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				limparTudo();
				return true;
			}
		});
	} // Fim do método onCreate

	// Sobrescrita do método onBackPressed para não voltar para
	// a activity SplashScreenActivity e ir para a tela inicial
	@Override
	public void onBackPressed() {
		this.moveTaskToBack(true);
	} // Fim do método onBackPressed

	// Método que será chamado ao clicar no botão '=' e que irá realizar o
	// cálculo
	public void calcular(View v) {
		try {
			Double res;
			StringBuilder stringBuilder = new StringBuilder(visor.getText()
					.toString().replace(getString(R.string.subtrair), "-")
					.replace(getString(R.string.multiplicar), "*")
					.replace(getString(R.string.dividir), "/")
					.replace(",", ".")
					.replace(getString(R.string.infinity), "$")
					.replace(getString(R.string.ninfinity), "§"));
			for (int i = stringBuilder.length() - 1; i >= 0; i--) {
				// System.out.println(stringBuilder.charAt(i));
				if (stringBuilder.charAt(i) == '.') {
					if (i > 0) {
						char c = stringBuilder.charAt(i - 1);
						if (c == '+' || c == '-' || c == '*' || c == '/') {
							stringBuilder.insert(i, "0");
						}
					} else {
						stringBuilder.insert(0, "0");
					}
				}
			}

			// Log.println(Log.DEBUG, "stringBuilderVisor",
			// stringBuilder.toString());

			res = eval(stringBuilder.toString());

			if (res.isNaN()) {
				visor.setTextColor(getResources().getColor(R.color.red));
				instRes.setTextColor(getResources().getColor(R.color.red));
				instRes.setText(getString(R.string.nan));
				speech.speak(getString(R.string.nan), TextToSpeech.QUEUE_FLUSH,
						null);
				return;
			} else if (res == Double.NEGATIVE_INFINITY) {
				visor.setText(getString(R.string.subtrair)
						+ getString(R.string.infinity));
				speech.speak(getString(R.string.subtrair)
						+ getString(R.string.infinity),
						TextToSpeech.QUEUE_FLUSH, null);
			} else if (res == Double.POSITIVE_INFINITY) {
				visor.setText(getString(R.string.infinity));
				speech.speak(getString(R.string.infinity),
						TextToSpeech.QUEUE_FLUSH, null);
			} else if (parteFracionaria(res) == 0.0) {
				visor.setText(String.valueOf(res.intValue())
						.replace("-", getString(R.string.subtrair))
						.replace(".", getString(R.string.separador)));
				speech.speak(String.valueOf(res.intValue()),
						TextToSpeech.QUEUE_FLUSH, null);
			} else {
				visor.setText(String.valueOf(res)
						.replace("-", getString(R.string.subtrair))
						.replace(".", getString(R.string.separador)));
				speech.speak(String.valueOf(res), TextToSpeech.QUEUE_FLUSH,
						null);
			}
			if (visor.getText().toString().length() > 11) {
				instRes.setText("––>");
			} else {
				instRes.setText("");
			}
		} catch (NumberFormatException e) {
			visor.setTextColor(getResources().getColor(R.color.red));
			instRes.setTextColor(getResources().getColor(R.color.red));
			instRes.setText(getString(R.string.nan));
			speech.speak(getString(R.string.nan), TextToSpeech.QUEUE_FLUSH,
					null);
		} catch (Exception e) {

			if (!e.getMessage().contains("Unexpected")) {
				visor.setTextColor(getResources().getColor(R.color.red));
				instRes.setTextColor(getResources().getColor(R.color.red));
				instRes.setText(getString(R.string.erro));
				speech.speak(getString(R.string.erro),
						TextToSpeech.QUEUE_FLUSH, null);
			}
			// Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),
			// Toast.LENGTH_SHORT).show();

		}
	} // Fim do método calcular

	// Método que recebe um valor e retorna a sua parte inteira
	// ex.: entrada = 12.234 | saída = 12.0
	double parteInteira(double valor) {
		if (valor >= 0.0) {
			return Math.floor(valor);
		} else {
			return Math.ceil(valor);
		}
	} // Fim do método parteInteira

	// Método que recebe um valor e retorna a sua parte fracionária
	// ex.: entrada = 12.234 | saída = 0.234
	double parteFracionaria(double valor) {
		if (valor >= 0.0) {
			return valor - Math.floor(valor);
		} else {
			return valor - Math.ceil(valor);
		}
	} // Fim do método parteFracionaria

	// Método que avalia/calcula o valor passado em uma String
	// ex.: entrada = "2+6*-2/32" | saída = 1.625
	public static double eval(final String str) {
		return new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while (ch == ' ') {
					nextChar();
				}
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			double parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length()) {
					throw new RuntimeException("Unexpected: " + (char) ch);
				}
				return x;
			}

			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)`
			// | number | functionName factor | factor `^` factor
			double parseExpression() {
				double x = parseTerm();
				for (;;) {
					if (eat('+')) {
						x += parseTerm(); // addition
					} else if (eat('-')) {
						x -= parseTerm(); // subtraction
					} else {
						return x;
					}
				}
			}

			double parseTerm() {
				double x = parseFactor();
				for (;;) {
					if (eat('*')) {
						x *= parseFactor(); // multiplication
					} else if (eat('/')) {
						x /= parseFactor(); // division
					} else {
						return x;
					}
				}
			}

			double parseFactor() {
				if (eat('§')) {
					return Double.NEGATIVE_INFINITY; // infinity
				}
				if (eat('$')) {
					return Double.POSITIVE_INFINITY; // infinity
				}
				if (eat('+')) {
					return parseFactor(); // unary plus
				}
				if (eat('-')) {
					return -parseFactor(); // unary minus
				}
				double x;
				int startPos = this.pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
					while ((ch >= '0' && ch <= '9') || ch == '.') {
						nextChar();
					}
					x = Double.parseDouble(str.substring(startPos, this.pos));
				} else if (ch >= 'a' && ch <= 'z') { // functions
					while (ch >= 'a' && ch <= 'z') {
						nextChar();
					}
					String func = str.substring(startPos, this.pos);
					x = parseFactor();
					if (func.equals("sqrt")) {
						x = Math.sqrt(x);
					} else if (func.equals("sin")) {
						x = Math.sin(Math.toRadians(x));
					} else if (func.equals("cos")) {
						x = Math.cos(Math.toRadians(x));
					} else if (func.equals("tan")) {
						x = Math.tan(Math.toRadians(x));
					} else {
						throw new RuntimeException("Unknown function: " + func);
					}
				} else {
					throw new RuntimeException("Unexpected: " + (char) ch);
				}

				if (eat('^')) {
					x = Math.pow(x, parseFactor()); // exponentiation
				}
				return x;
			}
		}.parse();
	} // Fim do método eval

	// Método que adiciona uma String 'toAdd' em outra String 'string'
	// e faz uma chamada do método speak da classe TextToSpeech
	// com o texto da String 'fala'
	public void add(String string, String toAdd, String fala) {
		if (string.endsWith(getString(R.string.subtrair))
				&& toAdd == getString(R.string.subtrair)) {
			return;
		} else if ((string.endsWith(getString(R.string.multiplicar))
				|| string.endsWith(getString(R.string.dividir))
				|| string.endsWith(getString(R.string.somar)) || string
					.endsWith(getString(R.string.subtrair)))
				&& !toAdd.equals(getString(R.string.subtrair))) {
			visor.setText(string.substring(0, string.length() - 1) + toAdd);
		} else {
			visor.append(toAdd);
		}
		if (fala != null) {
			speech.speak(fala, TextToSpeech.QUEUE_FLUSH, null);
		}
	} // Fim do método add

	// Método que será chamado ao clicar em algum número, sinal de operação ou
	// vírgula/ponto
	// no teclado da calculadora e que servirá para manipular os caracteres
	// inseridos no visor
	public void append(View v) {
		visor.setTextColor(getResources().getColor(R.color.dark_gray));
		instRes.setTextColor(getResources().getColor(R.color.gray));
		instRes.setText("");
		String tag = v.getTag().toString();
		String texto = visor.getText().toString();
		if (tag.equals(getString(R.string.dividir))) {
			if (texto.isEmpty()
					|| (texto.length() == 1 && texto.charAt(texto.length() - 1) == getString(
							R.string.subtrair).charAt(0))) {
				return;
			} else if (texto.length() > 1) {
				if ((texto.charAt(texto.length() - 2) == '+'
						|| texto.charAt(texto.length() - 2) == '÷' || texto
						.charAt(texto.length() - 2) == '×')
						&& texto.charAt(texto.length() - 1) == getString(
								R.string.subtrair).charAt(0)) {
					texto = texto.substring(0, texto.length() - 2)
							+ getString(R.string.dividir);
				} else if ((texto.charAt(texto.length() - 2) == '+'
						|| texto.charAt(texto.length() - 2) == '÷' || texto
						.charAt(texto.length() - 2) == '×')
						&& (texto.charAt(texto.length() - 1) == '+'
								|| texto.charAt(texto.length() - 1) == getString(
										R.string.subtrair).charAt(0)
								|| texto.charAt(texto.length() - 1) == '÷' || texto
								.charAt(texto.length() - 1) == '×')) {
					return;
				}
			}
			add(texto, tag, getString(R.string.dividir_fala));
		} else if (tag.equals(getString(R.string.multiplicar))) {
			if (texto.isEmpty()
					|| (texto.length() == 1 && texto.charAt(texto.length() - 1) == getString(
							R.string.subtrair).charAt(0))) {
				return;
			} else if (texto.length() > 1) {
				if ((texto.charAt(texto.length() - 2) == '+'
						|| texto.charAt(texto.length() - 2) == '÷' || texto
						.charAt(texto.length() - 2) == '×')
						&& texto.charAt(texto.length() - 1) == getString(
								R.string.subtrair).charAt(0)) {
					texto = texto.substring(0, texto.length() - 2)
							+ getString(R.string.multiplicar);
				} else if ((texto.charAt(texto.length() - 2) == '+'
						|| texto.charAt(texto.length() - 2) == '÷' || texto
						.charAt(texto.length() - 2) == '×')
						&& (texto.charAt(texto.length() - 1) == '+'
								|| texto.charAt(texto.length() - 1) == getString(
										R.string.subtrair).charAt(0)
								|| texto.charAt(texto.length() - 1) == '÷' || texto
								.charAt(texto.length() - 1) == '×')) {
					return;
				}
			}
			add(texto, tag, getString(R.string.multiplicar_fala));
		} else if (tag.equals("-")) {
			speech.speak(getString(R.string.subtrair_fala),
					TextToSpeech.QUEUE_FLUSH, null);
			if (texto.length() > 1) {
				if (((texto.charAt(texto.length() - 2) == '+'
						|| texto.charAt(texto.length() - 2) == '÷' || texto
						.charAt(texto.length() - 2) == '×') && (texto
						.charAt(texto.length() - 1) == '+'
						|| texto.charAt(texto.length() - 1) == getString(
								R.string.subtrair).charAt(0)
						|| texto.charAt(texto.length() - 1) == '÷' || texto
						.charAt(texto.length() - 1) == '×'))
						|| texto.charAt(texto.length() - 1) == '+') {
					visor.setText(texto.substring(0, texto.length() - 1)
							+ getString(R.string.subtrair));
					return;
				}
			}
			add(texto, getString(R.string.subtrair), null);
		} else if (tag.equals(getString(R.string.somar))) {
			if (texto.isEmpty()
					|| (texto.length() == 1 && texto.charAt(texto.length() - 1) == getString(
							R.string.subtrair).charAt(0))) {
				return;
			} else if (texto.length() > 1) {
				if ((texto.charAt(texto.length() - 2) == '+'
						|| texto.charAt(texto.length() - 2) == '÷' || texto
						.charAt(texto.length() - 2) == '×')
						&& texto.charAt(texto.length() - 1) == getString(
								R.string.subtrair).charAt(0)) {
					texto = texto.substring(0, texto.length() - 2)
							+ getString(R.string.somar);
				} else if ((texto.charAt(texto.length() - 2) == '+'
						|| texto.charAt(texto.length() - 2) == '÷' || texto
						.charAt(texto.length() - 2) == '×')
						&& (texto.charAt(texto.length() - 1) == '+'
								|| texto.charAt(texto.length() - 1) == getString(
										R.string.subtrair).charAt(0)
								|| texto.charAt(texto.length() - 1) == '÷' || texto
								.charAt(texto.length() - 1) == '×')) {
					return;
				}
			}
			add(texto, tag, getString(R.string.somar_fala));
		} else if (tag.equals(getString(R.string.separador))
				&& !texto.endsWith(getString(R.string.infinity))) {
			try {
				int qtdSeparador = 0;
				loopFor: for (int i = texto.length() - 1; i >= 0; i--) {
					// System.out.println(texto.charAt(i));
					if (texto.charAt(i) == getString(R.string.separador)
							.charAt(0)) {
						// System.out.println("separador");
						qtdSeparador++;
					} else if (texto.charAt(i) == '+'
							|| texto.charAt(i) == getString(R.string.subtrair)
									.charAt(0) || texto.charAt(i) == '×'
							|| texto.charAt(i) == '÷') {
						break loopFor;
					}
				}
				// System.out.println(qtdSeparador);
				if (qtdSeparador > 0) {
					return;
				} else {
					visor.setText(texto.substring(0, texto.length())
							+ getString(R.string.separador));
					speech.speak(tag, TextToSpeech.QUEUE_FLUSH, null);
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
						e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
			}
		} else if (!texto.endsWith(getString(R.string.infinity))) {
			visor.append(tag);

			if (visor.getText().toString().length() > 1) {
				setInstantRes(visor.getText().toString());
			} else {
				instRes.setText("");
			}

			speech.speak(tag, TextToSpeech.QUEUE_FLUSH, null);
		}
		visor.setSelection(visor.getText().toString().length());

	} // Fim do método append

	// Método para fazer um cálculo "instantâneo", ou seja,
	// exibir o resultado em um visor/TextView secundário
	// antes mesmo de clicar no botão '='
	private void setInstantRes(String valor) {
		try {
			if (!valor.endsWith(getString(R.string.multiplicar))
					&& !valor.endsWith(getString(R.string.dividir))
					&& !valor.endsWith(getString(R.string.somar))
					&& !valor.endsWith(getString(R.string.subtrair))
					&& (valor.contains(getString(R.string.multiplicar))
							|| valor.contains(getString(R.string.dividir))
							|| valor.contains(getString(R.string.somar)) || valor
								.contains(getString(R.string.subtrair)))) {
				Double res = eval(visor.getText().toString()
						.replace(getString(R.string.subtrair), "-")
						.replace(getString(R.string.multiplicar), "*")
						.replace(getString(R.string.dividir), "/")
						.replace(",", ".")
						.replace(getString(R.string.infinity), "$")
						.replace(getString(R.string.ninfinity), "§"));

				if (res.isNaN()) {
					visor.setTextColor(getResources().getColor(R.color.red));
					instRes.setTextColor(getResources().getColor(R.color.red));
					instRes.setText(getString(R.string.nan));
					return;
				} else if (res == Double.NEGATIVE_INFINITY) {
					instRes.setText(getString(R.string.subtrair)
							+ getString(R.string.infinity));
				} else if (res == Double.POSITIVE_INFINITY) {
					instRes.setText(getString(R.string.infinity));
				} else if (parteFracionaria(res) == 0.0) {
					instRes.setText(String.valueOf(res.intValue())
							.replace("-", getString(R.string.subtrair))
							.replace(".", getString(R.string.separador)));
				} else {
					instRes.setText(String.valueOf(res)
							.replace("-", getString(R.string.subtrair))
							.replace(".", getString(R.string.separador)));
				}
			} else {
				instRes.setText("");
			}
		} catch (NumberFormatException e) {
			instRes.setText("");
			// visor.setTextColor(getResources().getColor(R.color.red));
			// instRes.setTextColor(getResources().getColor(R.color.red));
			// instRes.setText(getString(R.string.nan));
		} catch (Exception e) {
			instRes.setText("");
			// visor.setTextColor(getResources().getColor(R.color.red));
			// instRes.setTextColor(getResources().getColor(R.color.red));
			// instRes.setText(getString(R.string.erro));
			/*
			 * Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),
			 * Toast.LENGTH_SHORT).show();
			 */
		}
	} // Fim do método setInstantRes

	// Método para remover o último caracter inserido na tela
	public void limpar(View v) {
		visor.setTextColor(getResources().getColor(R.color.dark_gray));
		instRes.setTextColor(getResources().getColor(R.color.gray));
		String valor = visor.getText().toString();
		if (valor.length() > 0) {
			valor = valor.substring(0, valor.length() - 1);
			visor.setText(valor);
			visor.setSelection(valor.length());

			if (instRes.getText().toString().equals("––>")) {
				if (valor.length() <= 11) {
					instRes.setText("");
				}
			} else {
				setInstantRes(valor);
			}
		}
	} // Fim do método limpar

	// Método para limpar todos os caracteres inseridos na tela
	public void limparTudo() {
		visor.setTextColor(getResources().getColor(R.color.dark_gray));
		instRes.setTextColor(getResources().getColor(R.color.gray));
		visor.setText("");
		instRes.setText("");
		speech.stop();
		speech.speak(getString(R.string.limpar_fala), TextToSpeech.QUEUE_FLUSH,
				null);
	} // Fim do método limparTudo

} // Fim da classe
