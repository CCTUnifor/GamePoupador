package algoritmo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import controle.Constantes;

public class Ladrao extends ProgramaLadrao {
	private short[][] mapa = new short[30][30];
	private List<int[][]> caminhos = new ArrayList<int[][]>();

	private int nMoedasAnt =0;
	private Point posAnterior = new Point();
    private int contContrario = 0;

	Ladrao() {
		criaCaminhos();
	}

	private final int CIMA = 1;
    private final int BAIXO = 2;
    private final int DIREITA = 3;
    private final int ESQUERDA = 4;

	class Destino {
		Destino(int direcao, int peso) {
			this.direcao = direcao;
			this.peso = peso;
		}

		int direcao;
		int peso;
	}

	public int acao() {
		int[][] matriz = matrizComTodosPesos();

		List<Destino> destinos = new ArrayList<Destino>();

		// Direita
		destinos.add(new Destino(DIREITA, menorPesoDireita(matriz)));

		// Cima
		rotacionaMatriz(matriz);
		int pesoAux = menorPesoDireita(matriz);
		int menorPeso = destinos.get(0).peso;
		if (pesoAux <= menorPeso) {
			if (pesoAux < menorPeso) {
				destinos.clear();
			}
			destinos.add(new Destino(CIMA, pesoAux));
		}

		// Esquerda
		rotacionaMatriz(matriz);
		pesoAux = menorPesoDireita(matriz);
		menorPeso = destinos.get(0).peso;
		if (pesoAux <= menorPeso) {
			if (pesoAux < menorPeso) {
				destinos.clear();
			}
			destinos.add(new Destino(ESQUERDA, pesoAux));
		}

		// Baixo
		rotacionaMatriz(matriz);
		pesoAux = menorPesoDireita(matriz);
		menorPeso = destinos.get(0).peso;
		if (pesoAux <= menorPeso) {
			if (pesoAux < menorPeso) {
				destinos.clear();
			}
			destinos.add(new Destino(BAIXO, pesoAux));
		}

		// Define a direção
		int destinoPos = (destinos.size() == 1) ? 0 : random(destinos.size() - 1);
		int direcao = destinos.get(destinoPos).direcao;

		// Marca no mapa a posição atual
		Point pos = sensor.getPosicao();
		mapa[pos.y][pos.x]++;

		if(isPrendendoPoupador(direcao)){
			contContrario += 5;
		}

		if (contContrario > 0) {
			direcao = inverterDirecao(direcao);
            contContrario--;
		}

        nMoedasAnt = sensor.getNumeroDeMoedas();
		posAnterior = pos;
		return direcao;
	}

	/**
	* Define se está prendendo um poupador
	 **/
	private boolean isPrendendoPoupador(int direcao){
	    if(nMoedasAnt < sensor.getNumeroDeMoedas()){
	        return true;
        }

        if(posAnterior.equals(sensor.getPosicao())){
            int codProxDirecao = -1;
            switch (direcao){
                case DIREITA: codProxDirecao = 12; break;

                case ESQUERDA: codProxDirecao = 11; break;

                case CIMA: codProxDirecao = 7; break;

                case BAIXO: codProxDirecao = 16; break;
            }
            if(codProxDirecao > -1) {
                int[] visao = sensor.getVisaoIdentificacao();
                return isPoupador(visao[codProxDirecao]);
            }
        }

        return false;
    }

	/**
	 * Acha o caminho com menor peso andando pela direita na matriz, e retorna o
	 * seu peso. Para fazer o mesmo para outro lado, basta rotacionar a matriz
	 * com rotacionaMatriz()
	 **/
	private int menorPesoDireita(int[][] matriz) {
		int x = 2;
		int y = 2;
		int menor = Integer.MAX_VALUE;
		for (int[][] caminho : caminhos) {
			int pesoAtual = 0;
			for (int[] passo : caminho) {
				pesoAtual += matriz[x + passo[1]][y + passo[0]] + 1;
			}
			if (pesoAtual < menor) {
				menor = pesoAtual;
			}
		}
		return menor;
	}

	/**
	 * Retorna a adição dos pesos do olfato e da parte visível do mapa aos
	 * pesos da visão
	 */
	private int[][] matrizComTodosPesos() {
		int[][] visao = pesarSensor(sensor.getVisaoIdentificacao(), 5, true);
		int[][] olfato = pesarSensor(sensor.getAmbienteOlfatoPoupador(), 3, false);

		// Sobrepõe a matriz de olfato na matriz de visão, somando seus pesos
		for (int i = 0; i < olfato.length; i++) {
			for (int j = 0; j < olfato.length; j++) {
				visao[i + 1][j + 1] += olfato[i][j];
			}
		}

		// Sobrepõe o mapa na matriz de visão, somando os pesos
		Point pos = sensor.getPosicao();
		for (int i = 0; i < visao.length; i++) {
			int x = pos.y + (i - 2);
			for (int j = 0; j < visao.length; j++) {
				int y = pos.x + (j - 2);
				if ((x >= 0 && x < mapa.length) && (y >= 0 && y < mapa[0].length)) {
					visao[i][j] += (int) mapa[x][y];
				}
			}
		}

		return visao;
	}

	/**
	 * Recebe um vetor de um sensor e transforma ele numa matriz quadrada com o
	 * tamanho passado. Os valores da matriz gerada são pesados com a função
	 * getPeso().
	 */
	private int[][] pesarSensor(int[] vetor, int tamanhoMatriz, boolean visao) {
		int[][] matrizComPeso = new int[tamanhoMatriz][tamanhoMatriz];

		int x = 0;
		int y = 0;

		int centro = tamanhoMatriz / 2;
		for (int i = 0; i < vetor.length; i++) {
			if (y == centro && x == centro) {
				// Vetor não vem com o valor do centro
				matrizComPeso[centro][centro] = 0;
				i--;
			} else {
				if (visao) {
					matrizComPeso[x][y] = getPeso(vetor[i]);
				} else {
					matrizComPeso[x][y] = (vetor[i] == 0) ? 0 : (vetor[i] - 6) * 10;
				}
			}

			if (y < tamanhoMatriz - 1) {
				y++;
			} else {
				y = 0;
				x++;
			}
		}

		return matrizComPeso;
	}

	/**
	 * Retorna o peso baseado num código. 0 significa espaço vazio, -100
	 * significa um poupador, Qualquer outra coisa é considerado um obstáculo
	 * e recebe um número bem alto.
	 **/
	private int getPeso(int cod) {
		if (isVazio(cod)) {
			return 0;
		} else if (isPoupador(cod)) {
			return -100;
		} else {
			return Integer.MAX_VALUE / 9;
		}
	}

	private boolean isPoupador(int cod) {
		return cod >= 100 && cod < 200;
	}

	private boolean isParede(int cod){
	    return !isPoupador(cod) && !isVazio(cod);
    }

	private boolean isVazio(int cod) {
		return cod == Constantes.posicaoLivre;
	}

	private int random(int max) {
		return ThreadLocalRandom.current().nextInt(0, max + 1);
	}
	
	private int inverterDirecao(int direcao){
		int novaDirecao = 0;
		int[] visao = sensor.getVisaoIdentificacao();

		switch (direcao){
			case CIMA:
			    if(!isParede(visao[16])) {
                    novaDirecao = BAIXO;
                }
				break;

			case BAIXO:
                if(!isParede(visao[7])) {
                    novaDirecao = CIMA;
                }
				break;

			case DIREITA:
                if(!isParede(visao[11])) {
                    novaDirecao = ESQUERDA;
                }
				break;

			case ESQUERDA:
                if(!isParede(visao[12])) {
                    novaDirecao = DIREITA;
                }
				break;
		}
		
		return novaDirecao;
		
	}

	/**
	 * Gira a matriz 90 graus para a direita
	 **/
	
	private void rotacionaMatriz(int[][] matriz) {
		int temp;

		// Transposta
		for (int i = 0; i < matriz.length; i++) {
			for (int j = i; j < matriz[0].length; j++) {
				temp = matriz[j][i];
				matriz[j][i] = matriz[i][j];
				matriz[i][j] = temp;
			}
		}

		// Inverte as colunas
		for (int i = 0; i < matriz[0].length; i++) {
			for (int j = 0, k = matriz[0].length - 1; j < k; j++, k--) {
				temp = matriz[i][j];
				matriz[i][j] = matriz[i][k];
				matriz[i][k] = temp;
			}
		}
	}

	private void printMatriz(int[][] matriz) {
		for (int i = 0; i < matriz.length; i++) {
			for (int j = 0; j < matriz.length; j++) {
				int c = matriz[j][i];
				String s = (c > 100) ? "x" : Integer.toString(c);
				System.out.print(s + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	 * Inicializa os caminhos que vão ser usados para checar no
	 * menorPesoDireita(). Esses caminhos são caminhos para posiçòes que
	 * seriam interessantes de chegar por um lado.
	 **/
	private void criaCaminhos() {
		caminhos.add(new int[][] { { 1, 0 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, 1 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, -1 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, 1 }, { 1, 2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, -1 }, { 1, -2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, 1 }, { 1, 2 }, { 2, 2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, -1 }, { 1, -2 }, { 2, -2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, 1 }, { 1, 2 }, { 0, 2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, -1 }, { 1, -2 }, { 0, -2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, 1 }, { 2, 1 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, -1 }, { 2, -1 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, 1 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, -1 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, 1 }, { 2, 2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, -1 }, { 2, -2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, 1 }, { 2, 2 }, { 1, 2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, -1 }, { 2, -2 }, { 1, -2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, 1 }, { 2, 2 }, { 1, 2 }, { 0, 2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, -1 }, { 2, -2 }, { 1, -2 }, { 0, -2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, 1 }, { 2, 2 }, { 1, 2 }, { 0, 2 }, { -1, 2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, -1 }, { 2, -2 }, { 1, -2 }, { 0, -2 }, { -1, -2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, 1 }, { 2, 2 }, { 1, 2 }, { 0, 2 }, { -1, 2 }, { -2, 2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 2, 0 }, { 2, -1 }, { 2, -2 }, { 1, -2 }, { 0, -2 }, { -1, -2 }, { -2, -2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, 1 }, { 1, 2 }, { 0, 2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, -1 }, { 1, -2 }, { 0, -2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, 1 }, { 1, 2 }, { 0, 2 }, { -1, 2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, -1 }, { 1, -2 }, { 0, -2 }, { -1, -2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, 1 }, { 1, 2 }, { 0, 2 }, { -1, 2 }, { -2, 2 } });
		caminhos.add(new int[][] { { 1, 0 }, { 1, -1 }, { 1, -2 }, { 0, -2 }, { -1, -2 }, { -2, -2 } });
	}

}