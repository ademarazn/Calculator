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
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {

	TextToSpeech speech;
	EditText visor, instRes;
	Button limpar;

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
	}

	public void calcular(View v) {
		try {
			double res;

			res = eval(visor.getText().toString()
					.replace(getString(R.string.subtrair), "-")
					.replace(getString(R.string.multiplicar), "*")
					.replace(getString(R.string.dividir), "/")
					.replace(",", "."));

			if (parteFracionaria(res) == 0.0) {
				visor.setText(String.valueOf((int) res)
						.replace("-", getString(R.string.subtrair))
						.replace(".", getString(R.string.separador)));
				speech.speak(String.valueOf((int) res),
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
		} catch (Exception e) {
			/*
			 * Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),
			 * Toast.LENGTH_SHORT).show();
			 */
		}
	}

	double parteInteira(double valor) {
		if (valor >= 0.0) {
			return Math.floor(valor);
		} else {
			return Math.ceil(valor);
		}
	}

	double parteFracionaria(double valor) {
		if (valor >= 0.0) {
			return valor - Math.floor(valor);
		} else {
			return valor - Math.ceil(valor);
		}
	}

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
	}

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
	}

	public void append(View v) {
		String tag = v.getTag().toString();
		String texto = visor.getText().toString();
		if (tag.equals(getString(R.string.dividir))) {
			if (texto.isEmpty()) {
				return;
			} else if (texto.length() > 1) {
				if ((texto.charAt(texto.length() - 2) == '+'
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
			if (texto.isEmpty()) {
				return;
			} else if (texto.length() > 1) {
				if ((texto.charAt(texto.length() - 2) == '+'
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
				if ((texto.charAt(texto.length() - 2) == '+'
						|| texto.charAt(texto.length() - 2) == '÷' || texto
						.charAt(texto.length() - 2) == '×')
						&& (texto.charAt(texto.length() - 1) == '+'
								|| texto.charAt(texto.length() - 1) == getString(
										R.string.subtrair).charAt(0)
								|| texto.charAt(texto.length() - 1) == '÷' || texto
								.charAt(texto.length() - 1) == '×')) {
					visor.setText(texto.substring(0, texto.length() - 1)
							+ getString(R.string.subtrair));
					return;
				}
			}
			add(texto, getString(R.string.subtrair), null);
		} else if (tag.equals(getString(R.string.somar))) {
			if (texto.isEmpty()) {
				return;
			} else if (texto.length() > 1) {
				if ((texto.charAt(texto.length() - 2) == '+'
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
			add(visor.getText().toString(), tag, getString(R.string.somar_fala));
		} else if (tag.equals(getString(R.string.separador))) {
			try {
				int qtdSeparador = 0;
				loopFor: for (int i = texto.length() - 1; i >= 0; i--) {
					System.out.println(texto.charAt(i));
					if (texto.charAt(i) == getString(R.string.separador)
							.charAt(0)) {
						System.out.println("separador");
						qtdSeparador++;
					} else if (texto.charAt(i) == '+'
							|| texto.charAt(i) == getString(R.string.subtrair)
									.charAt(0) || texto.charAt(i) == '×'
							|| texto.charAt(i) == '÷') {
						break loopFor;
					}
				}
				System.out.println(qtdSeparador);
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
		} else {
			visor.append(tag);

			if (visor.getText().toString().length() > 1) {
				setInstantRes(visor.getText().toString());
			} else {
				instRes.setText("");
			}

			speech.speak(tag, TextToSpeech.QUEUE_FLUSH, null);
		}
		visor.setSelection(visor.getText().toString().length());

	}

	public void limpar(View v) {
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
	}

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
				double res = eval(visor.getText().toString()
						.replace(getString(R.string.subtrair), "-")
						.replace(getString(R.string.multiplicar), "*")
						.replace(getString(R.string.dividir), "/")
						.replace(",", "."));

				if (parteFracionaria(res) == 0.0) {
					instRes.setText(String.valueOf((int) res)
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
		} catch (Exception e) {
			/*
			 * Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),
			 * Toast.LENGTH_SHORT).show();
			 */
		}
	}

	public void limparTudo() {
		visor.setText("");
		instRes.setText("");
		speech.stop();
		speech.speak(getString(R.string.limpar_fala), TextToSpeech.QUEUE_FLUSH,
				null);
	}

}
