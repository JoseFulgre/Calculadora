package fulgre.jose.calc.modelo;

import java.util.ArrayList;
import java.util.List;

public class Memoria {
	
	private enum TipoComando {
		ZERAR, NUMERO, DIV, MULT, SUB, SOMA, IGUAL, VIRGULA, PERCENT,POSNEG;
	}
	
	private static final Memoria instancia = new Memoria(); 
	
	private final List<MemoriaObservador> observadores = new ArrayList<>();
	
	private TipoComando ultimaOperacao = null;
	private boolean substituir = false;
	private String textoAtual = "";
	private String textoBuffer = "";

	private Memoria() {
		
	}
	

	public static Memoria getInstancia() {
		return instancia;
	}
	
	public void adicionarObservador(MemoriaObservador observador) {
		observadores.add(observador);
	}

	public String getTextoAtual() {
		return textoAtual.isEmpty() ? "0" : textoAtual;
	}
	
	public void processarComando(String texto) {
		
		TipoComando tipoComando = detectarTipoComando(texto);
		
		//System.out.println(tipo); //só para testes
		
		if(tipoComando == null) {
			return;
		} else if(tipoComando == TipoComando.ZERAR) {
			textoAtual = "";
			textoBuffer = "";
			substituir = false;
			ultimaOperacao = null;
		} else if(tipoComando == TipoComando.NUMERO 
				|| tipoComando == TipoComando.VIRGULA) {
			textoAtual = substituir ? texto : textoAtual + texto;
			substituir = false;
		} else if(tipoComando == TipoComando.POSNEG) {
			textoAtual = 
					!substituir && textoAtual.contains("-") ?
							textoAtual.substring(1) : "-" + textoAtual;
			substituir = false;
		} else {
			substituir = true;
			textoAtual = obterResultadoOperacao();
			textoBuffer = textoAtual;
			ultimaOperacao = tipoComando;
		}
				
		observadores.forEach(o -> o.valorAlterado(getTextoAtual()));
	}


	private String obterResultadoOperacao() {
		
		if(ultimaOperacao == null || ultimaOperacao == TipoComando.IGUAL) {
			return textoAtual;
		}
		
		double numeroBuffer = Double.parseDouble(textoBuffer.replace(",", "."));
		double numeroAtual = Double.parseDouble(textoAtual.replace(",", "."));
		
		Double resultado = 0.0;
		
		switch (ultimaOperacao) {
		case SOMA: 
			resultado = numeroBuffer + numeroAtual;
			break;
		
		case SUB: 
			resultado = numeroBuffer - numeroAtual;
			break;
			
		case MULT: 
			resultado = numeroBuffer * numeroAtual;
			break;
			
		case DIV: 
			resultado = numeroBuffer / numeroAtual;
			break;
			
		case PERCENT: 
			resultado = (numeroAtual * numeroBuffer) / 100;
			break;	
			
		default:
			;
		}
		String resultadoString = resultado.toString().replace(".", ",");
		boolean inteiro = resultadoString.endsWith(",0");
		return inteiro ? resultadoString.replace(",0", "") : resultadoString;
	}


	private TipoComando detectarTipoComando(String texto) {
		
		if(textoAtual.isEmpty() && texto.equals("0")) {
			return null;	
		}
		
		try {
			Integer.parseInt(texto);
			return TipoComando.NUMERO;			
		} catch (NumberFormatException e) {
			// Quando não for número
			
			if(",".equals(texto) && !textoAtual.contains(",")) {
				return TipoComando.VIRGULA;
			}
			
			switch (texto) {
			case "AC":
				return TipoComando.ZERAR;		
			case "÷":
				return TipoComando.DIV;		
			case "x":
				return TipoComando.MULT;			
			case "-":
				return TipoComando.SUB;		
			case "+":
				return TipoComando.SOMA;	
			case "=":
				return TipoComando.IGUAL;	
			case "%":
				return TipoComando.PERCENT;	
			case "±":
				return TipoComando.POSNEG;	
			default:
				return null;
			}
		}
		
	}
	
}
