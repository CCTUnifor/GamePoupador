// Ladrão Miguel
//

package algoritmo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Ladrao extends ProgramaLadrao {

	Integer[][] pesos = new Integer[30][30];
	Integer[][] conhecimento = new Integer[30][30];
	Point p;

	double explorar;
	double outraAcao;
	double chance;
	double cima;
	double baixo;
	double esquerda;
	double direita;
	double total;
	int cooldown;
	int cooldownValue = 3;
	int movimentoAnterior;
	
	Point banco = new Point(-1,-1);

	double poupadorLonge = 1.8;
	double poupadorPerto = 2.2;

	double olfato1 = 2.2;
	double olfato2 = 1.8;
	double olfato3 = 0.8;
	double olfato4 = 0.6;
	double olfato5 = 0.4;

	double chanceBancoX;
	double chanceBancoY;
	double chanceBancoSoma;
	
	double chanceJuntar;

	Random r;

	int move;

	int moedas = 0;

	Point posAnterior;

	public Ladrao() {
		r = new Random();

		for (int i = 0; i < pesos.length; i++) {
			for (int j = 0; j < pesos.length; j++) {
				pesos[i][j] = 0;
			}
		}

		for (int i = 0; i < conhecimento.length; i++) {
			for (int j = 0; j < conhecimento.length; j++) {
				conhecimento[i][j] = 0;
			}
		}

		explorar = 0;
		outraAcao = 0;
		posAnterior = new Point();

		posAnterior.x = -1;
		posAnterior.y = -1;
	}

	public int acao() {

//		return 0;
		return ladraoMiguel();
	}
	
	public int ladraoMiguel() {
		p = sensor.getPosicao();

		pesos[p.y][p.x] += 1;

		chance = 0;
		outraAcao = 0;
		chanceJuntar = 0;
		explorar = 0;
		cima = 0;
		baixo = 0;
		esquerda = 0;
		direita = 0;
		total = 1;
		
		boolean juntar = false;
		for(int i = 0 ; i < sensor.getVisaoIdentificacao().length; i ++) {
			if(sensor.getVisaoIdentificacao()[i] != 200 && sensor.getVisaoIdentificacao()[i] != 210 && 
					sensor.getVisaoIdentificacao()[i] != 220 && sensor.getVisaoIdentificacao()[i] != 230) {
				juntar = true;
				break;
			}
		}
		
		if(banco.x != -1 && banco.y != -1) {
			double sub1 = banco.getX() - p.getX();
			double sub2 = banco.getY() - p.getY();
			if(sub1 == 0) {
				sub1 = 1;
			}
			
			if(sub2 == 0) {
				sub2 = 1;
			}
			
			chanceBancoX = 1 - (1/Math.abs(sub1));
			chanceBancoY = 1 - (1/Math.abs(sub2));
			chanceBancoSoma = chanceBancoX + chanceBancoY;
		}

		int cont = 0;
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				try {
					if (i == 0 && j == 0) {

					} else {
						conhecimento[p.y + i][p.x + j] = sensor.getVisaoIdentificacao()[cont];
						
						if(sensor.getVisaoIdentificacao()[cont] == 3) {
							banco.y = p.y + i;
							banco.x = p.x + j;
						}

						if (sensor.getVisaoIdentificacao()[cont] == 100
								|| sensor.getVisaoIdentificacao()[cont] == 110) {
							outraAcao += 2;
						}

						cont++;
					}
				} catch (Exception e) {

				}
			}
		}
		
		

		for (int i = 0; i < sensor.getAmbienteOlfatoPoupador().length; i++) {
			if (sensor.getAmbienteOlfatoPoupador()[i] == 1) {
				outraAcao += 1.8;
			}

			if (sensor.getAmbienteOlfatoPoupador()[i] == 2) {
				outraAcao += 1.6;
			}

			if (sensor.getAmbienteOlfatoPoupador()[i] == 3) {
				outraAcao += 1.2;
			}

			if (sensor.getAmbienteOlfatoPoupador()[i] == 4) {
				outraAcao += 1;
			}
			
			if (sensor.getAmbienteOlfatoPoupador()[i] == 5) {
				outraAcao += 0.8;
			}
		}

		if (outraAcao > 3) {
			outraAcao = 2.95;
		}

		explorar = 1 - outraAcao;
		
		if (moedas < sensor.getNumeroDeMoedas()) {
			cooldown = cooldownValue;
			moedas = sensor.getNumeroDeMoedas();
		}

		if (posAnterior.x != p.x || posAnterior.y != p.y) {
			posAnterior.x = p.x;
			posAnterior.y = p.y;
		} else {
			if (movimentoAnterior != 0) {
				switch (movimentoAnterior) {
				case 1:
					if (sensor.getVisaoIdentificacao()[7] == 0 || sensor.getVisaoIdentificacao()[7] == 100
							|| sensor.getVisaoIdentificacao()[7] == 110) {
						cooldown = cooldownValue;
					}
					break;

				case 2:
					if (sensor.getVisaoIdentificacao()[16] == 0 || sensor.getVisaoIdentificacao()[16] == 100
							|| sensor.getVisaoIdentificacao()[16] == 110) {
						cooldown = cooldownValue;
					}
					break;

				case 3:
					if (sensor.getVisaoIdentificacao()[11] == 0 || sensor.getVisaoIdentificacao()[11] == 100
							|| sensor.getVisaoIdentificacao()[11] == 110) {
						cooldown = cooldownValue;
					}
					break;

				case 4:
					if (sensor.getVisaoIdentificacao()[12] == 0 || sensor.getVisaoIdentificacao()[12] == 100
							|| sensor.getVisaoIdentificacao()[12] == 110) {
						cooldown = cooldownValue;
					}
					break;
				}
			}
		}

		if (cooldown > 0) {
			cooldown--;
			return explorar();
		}
		
		if(sensor.getVisaoIdentificacao()[7] == 100 || sensor.getVisaoIdentificacao()[7] == 110 && cooldown == 0) {
			return 1;
		}
		if(sensor.getVisaoIdentificacao()[16] == 100 || sensor.getVisaoIdentificacao()[16] == 110 && cooldown == 0) {
			return 2;
		}
		if(sensor.getVisaoIdentificacao()[11] == 100 || sensor.getVisaoIdentificacao()[11] == 110 && cooldown == 0) {
			return 4;
		}
		if(sensor.getVisaoIdentificacao()[12] == 100 || sensor.getVisaoIdentificacao()[12] == 110 && cooldown == 0) {
			return 3;
		}

		explorar *= 100;
		outraAcao *= 100;

		ArrayList<Integer> possibilidades = new ArrayList<Integer>();

		int total = (int) explorar + (int) outraAcao;

		for (int i = 0; i < total; i++) {
			if (explorar > 0) {
				possibilidades.add(0);
				explorar--;
			} else if (outraAcao > 0) {
				possibilidades.add(1);
				outraAcao--;
			}
		}

		Collections.shuffle(possibilidades);
		if (possibilidades.get(0) == 0) {
			move = explorar();
		} else {
			move = outraAcao();
		}

		movimentoAnterior = move;
		return move;
	}

	public int explorar() {

		Point menor = new Point();
		int menorValor = menorValor();
		int movimento = 0;
		ArrayList<Integer> possibilidades = new ArrayList<Integer>();

		try {
			if (pesos[p.y - 1][p.x] != null && pesos[p.y - 1][p.x] <= menorValor) {
				if (sensor.getVisaoIdentificacao()[7] == 0) {
					menor.setLocation(p.x, p.y);
					menorValor = pesos[p.y - 1][p.x];
					movimento = 1;
					possibilidades.add(1);
				}
			}
		} catch (Exception e) {

		}

		try {
			if (pesos[p.y + 1][p.x] != null && pesos[p.y + 1][p.x] <= menorValor) {
				if (sensor.getVisaoIdentificacao()[16] == 0) {
					menor.setLocation(p.x, p.y);
					menorValor = pesos[p.y + 1][p.x];
					movimento = 2;
					possibilidades.add(2);
				}
			}
		} catch (Exception e) {

		}

		try {
			if (pesos[p.y][p.x - 1] != null && pesos[p.y][p.x - 1] <= menorValor) {
				if (sensor.getVisaoIdentificacao()[11] == 0) {
					menor.setLocation(p.x, p.y);
					menorValor = pesos[p.y][p.x - 1];
					movimento = 4;
					possibilidades.add(4);
				}
			}
		} catch (Exception e) {

		}

		try {
			if (pesos[p.y][p.x + 1] != null && pesos[p.y][p.x + 1] <= menorValor) {
				if (sensor.getVisaoIdentificacao()[12] == 0) {
					menor.setLocation(p.x, p.y);
					menorValor = pesos[p.y][p.x + 1];
					movimento = 3;
					possibilidades.add(3);
				}
			}
		} catch (Exception e) {

		}

		if (movimento == 0) {
			
		}

		Collections.shuffle(possibilidades);
		try {
			movimento = possibilidades.get(0);
		} catch (Exception e) {

		}
		return movimento;
	}

	public int menorValor() {
		int menor = 10000;

		try {
			if (pesos[p.y - 1][p.x] != null && pesos[p.y - 1][p.x] < menor) {
				if (sensor.getVisaoIdentificacao()[7] == 0) {
					menor = pesos[p.y - 1][p.x];
				}
			}
		} catch (Exception e) {

		}

		try {
			if (pesos[p.y + 1][p.x] != null && pesos[p.y + 1][p.x] < menor) {
				if (sensor.getVisaoIdentificacao()[16] == 0) {
					menor = pesos[p.y + 1][p.x];
				}
			}
		} catch (Exception e) {

		}

		try {
			if (pesos[p.y][p.x - 1] != null && pesos[p.y][p.x - 1] < menor) {
				if (sensor.getVisaoIdentificacao()[11] == 0) {
					menor = pesos[p.y][p.x - 1];
				}
			}
		} catch (Exception e) {

		}

		try {
			if (pesos[p.y][p.x + 1] != null && pesos[p.y][p.x + 1] < menor) {
				if (sensor.getVisaoIdentificacao()[12] == 0) {
					menor = pesos[p.y][p.x + 1];
				}
			}
		} catch (Exception e) {

		}

		return menor;
	}

	public void exibirPesos() {
		for (int i = 0; i < pesos.length; i++) {
			for (int j = 0; j < pesos.length; j++) {
				if (p.y == i && p.x == j) {
					
				} else {
					
				}
			}
		}
	}

	public void exibirConhecimento() {
		for (int i = 0; i < conhecimento.length; i++) {
			for (int j = 0; j < conhecimento.length; j++) {
				if (p.y == i && p.x == j) {
				} else {
				}
			}
		}
	}

	public int outraAcao() {
		double cima = avaliarCima();
		double baixo = avaliarBaixo();
		double esquerda = avaliarEsquerda();
		double direita = avaliarDireita();


		if (cima < 0.0125) {
			cima = 0.0125;
		}

		if (baixo < 0.0125) {
			baixo = 0.0125;
		}

		if (esquerda < 0.0125) {
			esquerda = 0.0125;
		}

		if (direita < 0.0125) {
			direita = 0.0125;
		}

		cima *= 100;
		baixo *= 100;
		esquerda *= 100;
		direita *= 100;
		
		double total = cima + baixo + esquerda + direita;

		ArrayList<Integer> possibilidades = new ArrayList<Integer>();
		int qtdCima = (int) cima;
		int qtdBaixo = (int) baixo;
		int qtdEsquerda = (int) esquerda;
		int qtdDireita = (int) direita;

		for (int i = 0; i < (int) total; i++) {
			if (qtdCima > 0) {
				possibilidades.add(1);
				qtdCima--;
			} else if (qtdBaixo > 0) {
				possibilidades.add(2);
				qtdBaixo--;
			} else if (qtdEsquerda > 0) {
				possibilidades.add(4);
				qtdEsquerda--;
			} else if (qtdDireita > 0) {
				possibilidades.add(3);
				qtdDireita--;
			}
		}

		Collections.shuffle(possibilidades);
		return possibilidades.get(0);

	}

	public double avaliarCima() {
		// Avaliar vis�o
		for (int i = 0; i < 10; i++) {
			if (sensor.getVisaoIdentificacao()[i] == 100 || sensor.getVisaoIdentificacao()[i] == 110) {
				switch (i) {
				case 6:
					cima += poupadorPerto;
					break;

				case 7:
					cima += poupadorPerto;
					break;

				case 8:
					cima += poupadorPerto;
					break;

				default:
					cima += poupadorLonge;
					break;
				}
			}
		
		}
		
		if(sensor.getVisaoIdentificacao()[7] == 1 || sensor.getVisaoIdentificacao()[7] == 4) {
			cima -= 0.5;
		}
		
		boolean usar = true;

		for (int i = 0; i < sensor.getVisaoIdentificacao().length; i++) {
			if (sensor.getVisaoIdentificacao()[i] == 100 || sensor.getVisaoIdentificacao()[i] == 110) {
				usar = false;
				break;
			}
		}

		// Avaliar olfato
		if (usar) {
			for (int i = 0; i < 3; i++) {
				if (sensor.getAmbienteOlfatoPoupador()[i] == 1) {
					cima += olfato1;
				}

				if (sensor.getAmbienteOlfatoPoupador()[i] == 2) {
					cima += olfato2;
				}

				if (sensor.getAmbienteOlfatoPoupador()[i] == 3) {
					cima += olfato3;
				}

				if (sensor.getAmbienteOlfatoPoupador()[i] == 4) {
					cima += olfato4;
				}

				if (sensor.getAmbienteOlfatoPoupador()[i] == 5) {
					cima += olfato5;
				}
			}
			if(banco.getY() > p.getY()) {
				cima += chanceBancoY;
			}
		}

		return cima;
	}

	public double avaliarBaixo() {
		for (int i = 14; i < 24; i++) {
			if (sensor.getVisaoIdentificacao()[i] == 100 || sensor.getVisaoIdentificacao()[i] == 110) {
				switch (i) {
				case 15:
					baixo += poupadorPerto;
					break;

				case 16:
					baixo += poupadorPerto;
					break;

				case 17:
					baixo += poupadorPerto;
					break;

				default:
					baixo += poupadorLonge;
					break;
				}
			}
		
		}
		
		if(sensor.getVisaoIdentificacao()[16] == 1 || sensor.getVisaoIdentificacao()[16] == 4) {
			baixo -= 0.5;
		}
		
		boolean usar = true;

		for (int i = 0; i < sensor.getVisaoIdentificacao().length; i++) {
			if (sensor.getVisaoIdentificacao()[i] == 100 || sensor.getVisaoIdentificacao()[i] == 110) {
				usar = false;
				break;
			}
		}

		if (usar) {
			// Avaliar olfato
			for (int i = 5; i < 8; i++) {
				if (sensor.getAmbienteOlfatoPoupador()[i] == 1) {
					baixo += olfato1;
				}

				if (sensor.getAmbienteOlfatoPoupador()[i] == 2) {
					baixo += olfato2;
				}

				if (sensor.getAmbienteOlfatoPoupador()[i] == 3) {
					baixo += olfato3;
				}

				if (sensor.getAmbienteOlfatoPoupador()[i] == 4) {
					baixo += olfato4;
				}

				if (sensor.getAmbienteOlfatoPoupador()[i] == 5) {
					baixo += olfato5;
				}
			}
			if(banco.getY() < p.getY()) {
				baixo += chanceBancoY;
			}

		}

		return baixo;
	}

	public double avaliarEsquerda() {
		int i = 0;

		while (i < 20) {
			for (int j = i; j <= i + 1; j++) {
				if (sensor.getVisaoIdentificacao()[j] == 100 || sensor.getVisaoIdentificacao()[j] == 110) {
					switch (i) {
					case 6:
						esquerda += poupadorPerto;
						break;

					case 11:
						esquerda += poupadorPerto;
						break;

					case 15:
						esquerda += poupadorPerto;
						break;

					default:
						esquerda += poupadorLonge;
						break;
					}
				}
			}
			if (i == 10) {
				i += 4;
			} else {
				i += 5;
			}
		}
		
		if(sensor.getVisaoIdentificacao()[11] == 1 || sensor.getVisaoIdentificacao()[11] == 4) {
			esquerda -= 0.5;
		}

		boolean usar = true;

		for (int j = 0; j < sensor.getVisaoIdentificacao().length; j++) {
			if (sensor.getVisaoIdentificacao()[j] == 100 || sensor.getVisaoIdentificacao()[j] == 110) {
				usar = false;
				break;
			}
		}

		if (usar) {
			// Avaliar olfato
			int possibilidades[] = { 0, 3, 5 };

			for (int j = 0; j < possibilidades.length; j++) {
				if (sensor.getAmbienteOlfatoPoupador()[possibilidades[j]] == 1) {
					esquerda += olfato1;
				}
				if (sensor.getAmbienteOlfatoPoupador()[possibilidades[j]] == 2) {
					esquerda += olfato2;
				}
				if (sensor.getAmbienteOlfatoPoupador()[possibilidades[j]] == 3) {
					esquerda += olfato3;
				}
				if (sensor.getAmbienteOlfatoPoupador()[possibilidades[j]] == 4) {
					esquerda += olfato4;
				}
				if (sensor.getAmbienteOlfatoPoupador()[possibilidades[j]] == 5) {
					esquerda += olfato5;
				}
			}
			if(banco.getX() < p.getX()) {
				esquerda += chanceBancoX;
			}
		}

		return esquerda;
	}

	public double avaliarDireita() {
		int i = 3;

		while (i < 23) {
			for (int j = i; j <= i + 1; j++) {
				if (sensor.getVisaoIdentificacao()[j] == 100 || sensor.getVisaoIdentificacao()[j] == 110) {
					switch (i) {
					case 8:
						direita += poupadorPerto;
						break;

					case 12:
						direita += poupadorPerto;
						break;

					case 17:
						direita += poupadorPerto;
						break;

					default:
						direita += poupadorLonge;
						break;
					}
				}

			}
			if (i == 8) {
				i += 4;
			} else {
				i += 5;
			}
			
		}

		if(sensor.getVisaoIdentificacao()[12] == 1 || sensor.getVisaoIdentificacao()[12] == 4) {
			direita -= 0.5;
		}
		
		boolean usar = true;

		for (int j = 0; j < sensor.getVisaoIdentificacao().length; j++) {
			if (sensor.getVisaoIdentificacao()[j] == 100 || sensor.getVisaoIdentificacao()[j] == 110) {
				usar = false;
				break;
			}
		}
		
		if (usar) {
			// Avaliar olfato
			int possibilidades[] = { 2, 4, 7 };

			for (int j = 0; j < possibilidades.length; j++) {
				if (sensor.getAmbienteOlfatoPoupador()[possibilidades[j]] == 1) {
					direita += olfato1;
				}
				if (sensor.getAmbienteOlfatoPoupador()[possibilidades[j]] == 2) {
					direita += olfato2;
				}
				if (sensor.getAmbienteOlfatoPoupador()[possibilidades[j]] == 3) {
					direita += olfato3;
				}
				if (sensor.getAmbienteOlfatoPoupador()[possibilidades[j]] == 4) {
					direita += olfato4;
				}
				if (sensor.getAmbienteOlfatoPoupador()[possibilidades[j]] == 5) {
					direita += olfato5;
				}
			}
			if(banco.getX() > p.getX()) {
				direita += chanceBancoX;
			}
		}

		return direita;
	}
	
	public int seEncontrar() {
		//Cima
		for (int i = 0; i < 3; i++) {
			if (sensor.getAmbienteOlfatoLadrao()[i] == 1) {
				cima += olfato1;
			}

			if (sensor.getAmbienteOlfatoLadrao()[i] == 2) {
				cima += olfato2;
			}

			if (sensor.getAmbienteOlfatoLadrao()[i] == 3) {
				cima += olfato3;
			}

			if (sensor.getAmbienteOlfatoLadrao()[i] == 4) {
				cima += olfato4;
			}

			if (sensor.getAmbienteOlfatoLadrao()[i] == 5) {
				cima += olfato5;
			}
		}
		
		//baixo
		for (int i = 5; i < 8; i++) {
			if (sensor.getAmbienteOlfatoLadrao()[i] == 1) {
				baixo += olfato1;
			}

			if (sensor.getAmbienteOlfatoLadrao()[i] == 2) {
				baixo += olfato2;
			}

			if (sensor.getAmbienteOlfatoLadrao()[i] == 3) {
				baixo += olfato3;
			}

			if (sensor.getAmbienteOlfatoLadrao()[i] == 4) {
				baixo += olfato4;
			}

			if (sensor.getAmbienteOlfatoLadrao()[i] == 5) {
				baixo += olfato5;
			}
		}
		
		//esquerda
		int possibilidades[] = { 0, 3, 5 };

		for (int j = 0; j < possibilidades.length; j++) {
			if (sensor.getAmbienteOlfatoLadrao()[possibilidades[j]] == 1) {
				esquerda += olfato1;
			}
			if (sensor.getAmbienteOlfatoLadrao()[possibilidades[j]] == 2) {
				esquerda += olfato2;
			}
			if (sensor.getAmbienteOlfatoLadrao()[possibilidades[j]] == 3) {
				esquerda += olfato3;
			}
			if (sensor.getAmbienteOlfatoLadrao()[possibilidades[j]] == 4) {
				esquerda += olfato4;
			}
			if (sensor.getAmbienteOlfatoLadrao()[possibilidades[j]] == 5) {
				esquerda += olfato5;
			}
		}
		
		//direita
		int possibilidades2[] = { 2, 4, 7 };

		for (int j = 0; j < possibilidades2.length; j++) {
			if (sensor.getAmbienteOlfatoLadrao()[possibilidades2[j]] == 1) {
				direita += olfato1;
			}
			if (sensor.getAmbienteOlfatoLadrao()[possibilidades2[j]] == 2) {
				direita += olfato2;
			}
			if (sensor.getAmbienteOlfatoLadrao()[possibilidades2[j]] == 3) {
				direita += olfato3;
			}
			if (sensor.getAmbienteOlfatoLadrao()[possibilidades2[j]] == 4) {
				direita += olfato4;
			}
			if (sensor.getAmbienteOlfatoLadrao()[possibilidades2[j]] == 5) {
				direita += olfato5;
			}
		}
		
		if (cima == 0) {
			cima = 0.0125;
		}

		if (baixo == 0) {
			baixo = 0.0125;
		}

		if (esquerda == 0) {
			esquerda = 0.0125;
		}

		if (direita == 0) {
			direita = 0.0125;
		}

		cima *= 100;
		baixo *= 100;
		esquerda *= 100;
		direita *= 100;
		
		double total = cima + baixo + esquerda + direita;

		ArrayList<Integer> possibilidades3 = new ArrayList<Integer>();
		int qtdCima = (int) cima;
		int qtdBaixo = (int) baixo;
		int qtdEsquerda = (int) esquerda;
		int qtdDireita = (int) direita;

		for (int i = 0; i < (int) total; i++) {
			if (qtdCima > 0) {
				possibilidades3.add(1);
				qtdCima--;
			} else if (qtdBaixo > 0) {
				possibilidades3.add(2);
				qtdBaixo--;
			} else if (qtdEsquerda > 0) {
				possibilidades3.add(4);
				qtdEsquerda--;
			} else if (qtdDireita > 0) {
				possibilidades3.add(3);
				qtdDireita--;
			}
		}

		Collections.shuffle(possibilidades3);
		return possibilidades3.get(0);
	}
}