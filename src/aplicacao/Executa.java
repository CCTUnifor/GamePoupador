package aplicacao;

import gui.FramePrincipal;

public class Executa {

	public static void main(String[] args) {
		FramePrincipal jogo = new FramePrincipal();
		jogo.iniciarJogo();
	}
}

/*
// AÇÕES (criar enum)
private final int CIMA = 1;
private final int BAIXO = 2;
private final int DIREITA = 3;
private final int ESQUERDA = 4;

ITERAÇÃO{
	atualizar_mapa(getvisão());

	estados_possiveis = sucessor(estado_atual);	
	
	para cada Possibilidade p em estados_possiveis faça{
		p.peso = calcula_custo(p) // tupla {ação, posicao}
	}
	
	retorn Util.maiorpeso(possibilidades);
}

function sucessor(estado_atual){
	retorn lista de estados e as ações pra cada estado // lista de tamanho maximo 4, (tupla = Possibilidade => {ação, posicao, peso = 0})
}

function calcular_custo(Possibilidade p){
	int peso = 0
	//ao ver um ladrão o peso diminui drasticamente
	peso = calculo_do_fator_visão(p)
	
	//ao cheirar um ladrão o peso diminui de acordo com o tempo do cheiro, quanto maior o tempo menor a diminuição
	peso += calculo_do_fator_olfato(p)
	
	//quanto tempo faz que ele não visita uma região
	//alguma moeda deixada pra trás naquela direção
	peso += calculo_do_fator_exploração(p)
	
	return 3-tupla => {peso, ação, posicao}
}

function calculo_do_fator_visão(Possibilidade p){
	int peso = 0

	se vejo um ladrão então  
		o peso diminui drasticamente
		estado = fulga
	se não se vejo uma ou mais moedas então 
		o peso aumenta consideravelmente
	
	se não vejo um ladrão e estado == fulga então
		se vejo uma pastinha então
			peso aumenta	
	
	retorn peso;
}

function calculo_do_fator_olfato(Possibilidade p){
	int peso = 0

	se farejei algum ladrão 
		peso diminui mais quanto menor for o tempo do cheiro(1 a 5)

	retorno peso;
}

function calculo_do_fator_exploração(Possibilidade p){
	int peso = 0;
	recorte = recortar_mapa_de_acordo_com_a_direção_da_ação(mapa, p.ação); // recorte == [SEi, SEj, IDi, IDj ] (ponto Superior Esquerdo e ponto Inferior Direito do recorte)

	objetos_encontrados = varrer_mapa_procurando_objetos(recorte) // (moedas, pastinhas)
	
	if(objetos_encontrados.length > 0){
		para cada objeto em objetos_encontrados faça
		peso += objeto.peso.getValue() * objeto.distancia

	}else{
		bolar esquema de exploração
	}

	retorn peso;
}

class objeto{
	enumObjetos peso {private set; public get};
	float distancia = 0 {private set; public get};

	const(enumObjetos peso, float distancia){ 
		this.peso = peso; 
		this.distancia = distancia 
	}
}

class Possibilidade{
	enumAções ação;
	Point posicao;
	float peso = 0;
}


























 
 _____________________________________________________
 |000000011111111100000000000000000000000000000000000|
 |000011111000000000000000000000000000000000000000000|
 |000000000000000000000000000000000000000000000000000|
 |000000000000000000000000000000000000000000000000000|
 |000000000000000000000000000000000000000000000000000|
 |000000000000000000000000000000000000000000000000000|
 |000000000000000000000000000000000000000000000000000|
 |000000000000000000000000000000000000000000000000000|
 |000000000000000000000000000000000000000000000000000|
 |000000000000000000000000000000000000000000000000000|
 |000000000000000000000000000000000000000000000000000|
 |000000000000000000000000000000000000000000000000000|
 |000000000000000000000000000000000000000000000000000|
 _____________________________________________________
 
 
 
 baixo direita esquerda
 _________
 |	p	l
 |		l
 |
 |
 
 baixo direita
 ________
 |p	   l
 |	   l
 |
 |
   
   
   
 * */