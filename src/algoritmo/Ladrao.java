package algoritmo;

import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

public class Ladrao extends ProgramaLadrao {

    private LinkedList<Posicao> historicoDePosicaos = new LinkedList<>();
    private Point posicaoAtual;
    private int maiorPeso;
    private int direcao;
    private Posicao posicao;


    public int acao() {
        int direcaoVisaoLadrao = procurarPoupadorNaVisao();
        int direcaoOlfatoLadrao = procurarPoupadorNoOlfato();

        posicaoAtual = sensor.getPosicao();

        adicionarPontoVisitado(posicaoAtual);

        if(direcaoVisaoLadrao != 0) {
            //System.out.println("Modo: Perseguição Sensor: Visão Direção: " + direcaoVisaoLadrao);
            return direcaoVisaoLadrao;
        } else if(direcaoOlfatoLadrao != 0) {
            //System.out.println("Modo: Perseguição Sensor: Olfato Direção: " + direcaoOlfatoLadrao);
            return direcaoOlfatoLadrao;
        } else {
            direcao = explorar();
            //System.out.println("Modo: Exploração Direção: " + direcao);
            return direcao;
        }

    }

    private int procurarPoupadorNaVisao() {
        int[] visao = sensor.getVisaoIdentificacao();
        int[] pesos = {0, 0, 0, 0, 0};

        for(int i = 0; i < visao.length; i++) {
            if(i <= 9) {
                pesos[1] = avaliaCodigoVisao(visao[i]);
            } else if(i <= 11) {
                pesos[4] = avaliaCodigoVisao(visao[i]);
            } else if(i <= 13) {
                pesos[3] = avaliaCodigoVisao(visao[i]);
            } else if(i <= 23) {
                pesos[2] = avaliaCodigoVisao(visao[i]);
            }
        }

        maiorPeso = 0;
        direcao = 0;

        for(int i = 1; i < pesos.length; i++) {
            if(pesos[i] > maiorPeso) {
                maiorPeso = pesos[i];
                direcao = i;
            }
        }


        return direcao;
    }

    private int avaliaCodigoVisao(int visao) {
        int peso = 0;

        if(visao == 100) {
            peso += 1;
        }
        if(visao == 200) {
            peso -= 2;
        }

        return peso;
    }

    private int procurarPoupadorNoOlfato() {
        int[] olfatoPoupador = sensor.getAmbienteOlfatoPoupador();
        int[] pesos = new int[5];

        for(int i = 0; i < olfatoPoupador.length; i++) {
            if(olfatoPoupador[i] >= 1) {
                if (i <= 2) {
                    pesos[1] = compararPesoOlfato(pesos[1], olfatoPoupador[i]);
                } else if (i <= 3) {
                    pesos[4] = compararPesoOlfato(pesos[4], olfatoPoupador[i]);
                } else if (i <= 4) {
                    pesos[3] = compararPesoOlfato(pesos[3], olfatoPoupador[i]);
                } else if (i <= 7) {
                    pesos[2] = compararPesoOlfato(pesos[2], olfatoPoupador[i]);
                }
            }
        }

        maiorPeso = 5;
        direcao = 0;

        for(int i = 1; i < pesos.length; i++) {
            if(pesos[i] <= maiorPeso && pesos[i] != 0) {
                maiorPeso = pesos[i];
                direcao = i;
            }
        }


        return direcao;
    }

    private int compararPesoOlfato(int pesoAnterior, int novoPeso)	{
        if(novoPeso < pesoAnterior && novoPeso != 0 || pesoAnterior == 0) {
            return novoPeso;
        }

        return pesoAnterior;
    }


    private int explorar() {
        int[] visao = sensor.getVisaoIdentificacao();
        int[] direcoes = new int[5];

        direcoes[1] = avaliarMovimento(visao, 1);
        direcoes[2] = avaliarMovimento(visao, 2);
        direcoes[3] = avaliarMovimento(visao, 3);
        direcoes[4] = avaliarMovimento(visao, 4);

        Random random = new Random();

        maiorPeso = 0;
        direcao = 1 + random.nextInt(4);


        for(int i = 1; i < direcoes.length; i++) {
            if(direcoes[i] >= maiorPeso) {
                maiorPeso = direcoes[i];
                direcao = i;
            }
        }

        return direcao;
    }

    private int avaliarMovimento(int[] visao, int direcao) {
        int codigoPonto = 10;
        int peso = 0;
        Point pontoMapa = new Point();

        if(direcao == 1) {
            codigoPonto = visao[7];
            pontoMapa = new Point(posicaoAtual.x, posicaoAtual.y - 1);
        } else if(direcao == 2) {
            codigoPonto = visao[16];
            pontoMapa = new Point(posicaoAtual.x, posicaoAtual.y + 1);
        } else if(direcao == 3) {
            codigoPonto = visao[12];
            pontoMapa = new Point(posicaoAtual.x + 1, posicaoAtual.y);
        } else if(direcao == 4) {
            codigoPonto = visao[11];
            pontoMapa = new Point(posicaoAtual.x - 1, posicaoAtual.y);
        }

        peso -= procurarPontoVisitado(pontoMapa);

        if(codigoPonto == 1 || codigoPonto == 4 || codigoPonto == -2 || codigoPonto == -1 || codigoPonto == 3 || codigoPonto == 5) {
            peso -= 1;
        } else if(codigoPonto == 0) {
            peso += 3;
        }


        return peso;

    }


    private void adicionarPontoVisitado(Point ponto) {
        boolean encontrou = false;

        for(int i = 0; i < historicoDePosicaos.size(); i++) {

            posicao = historicoDePosicaos.get(i);

            if(posicao.x == ponto.x && posicao.y == ponto.y) {
                historicoDePosicaos.get(i).setPeso(posicao.getPeso() + 2);
                encontrou = true;
            }
        }

        if(!encontrou) {
            historicoDePosicaos.add(new Posicao(ponto.x, ponto.y, 1));
        }

    }

    private int procurarPontoVisitado(Point ponto) {
        for(int i = 0; i < historicoDePosicaos.size(); i++) {

            posicao = historicoDePosicaos.get(i);

            if(posicao.x == ponto.x && posicao.y == ponto.y) {
                return posicao.getPeso();
            }
        }

        return 0;
    }


}

class Posicao extends Point {
    private int peso;

    public Posicao(int x, int y, int peso) {
        super(x, y);
        this.peso = peso;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }
}

class Movimento {
    private int direcao;
    private int peso;

    public Movimento(int direcao, int peso) {
        this.direcao = direcao;
        this.peso = peso;
    }

    public int getDirecao() {
        return direcao;
    }

    public void setDirecao(int direcao) {
        this.direcao = direcao;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }
}