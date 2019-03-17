package algoritmo;

import algoritmo.domain.StateToMove;
import algoritmo.domain.Vision;
import algoritmo.domain.VisionEnum;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Poupador extends ProgramaPoupador {
	private boolean firstMove = true;
	private Vision vision;
	private List<StateToMove> availableCells;

	public int acao() {
		initializeVariables();
		updateVariables();


		perceber();
		interpretar();
		objetivos();
		estadoInterno();
		utilidade();
		realizarAcao();

		return 0;
	}

	private void initializeVariables() {
		if (!firstMove)
			return;
		vision = new Vision(sensor);

	}

	private void updateVariables() {
		vision.update();
		cognizeAvailableCell();
	}


	private void cognizeAvailableCell() {
		List<StateToMove> available = new ArrayList<>();

		VisionEnum[] availablesStates = new VisionEnum[]{
				VisionEnum.BANK,
				VisionEnum.COIN,
				VisionEnum.FLOOR,
				VisionEnum.POWER_UP,
		};

		for (VisionEnum e : availablesStates) {
			if (vision.getTop() == e)
				available.add(StateToMove.create(ActionEnum.UP, vision.getTop()));
			if (vision.getBottom() == e)
				available.add(StateToMove.create(ActionEnum.DOWN, vision.getBottom()));
			if (vision.getLeft() == e)
				available.add(StateToMove.create(ActionEnum.LEFT, vision.getLeft()));
			if (vision.getRight() == e)
				available.add(StateToMove.create(ActionEnum.RIGHT, vision.getRight()));
		}

		this.availableCells = available;
	}

	private void perceber() {

		int[] visaoIdentificacao = sensor.getVisaoIdentificacao();
		int[] ambienteOlfatoLadrao = sensor.getAmbienteOlfatoLadrao();
		int[] ambienteOlfatoPoupador = sensor.getAmbienteOlfatoPoupador();
		int numeroDeMoedas = sensor.getNumeroDeMoedas();
		int numeroDeMoedasBanco = sensor.getNumeroDeMoedasBanco();
		int numeroJogadasImunes = sensor.getNumeroJogadasImunes();
		Point posicao = sensor.getPosicao();

	}

	private void interpretar() {
	}

	private void objetivos() {
	}

	private void estadoInterno() {
	}

	private void utilidade() {
	}

	private void realizarAcao() {

	}
}