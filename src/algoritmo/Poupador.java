package algoritmo;

import algoritmo.domain.*;
import controle.Constantes;
import jdk.nashorn.internal.runtime.GlobalConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Poupador extends ProgramaPoupador {
	private boolean firstMove = true;
	private Vision vision;
	private List<StateToMove> availableCells;
	private List<Point> mapDiscovered;

	public int acao() {
		System.out.println("-----------------------------------------------------------");
		System.out.println("Poupador posição: " + sensor.getPosicao().toString());

		initializeVariables();
		updateMapDiscovered();
		cognize();
		System.out.println("Availables: " + availableCells.toString());
		searchForCoins();
		// filterByHasCoins();
		System.out.println("Availables after coins: " + availableCells.toString());

		StateToMove chosed = choosePath();
		System.out.println("Chose: " + chosed.toString());

		System.out.println("-----------------------------------------------------------\n");
		return chosed.getAction().getValue();
	}

	private void initializeVariables() {
		if (!firstMove){
			vision.setSensor(sensor);
			return;
		}
		firstMove = false;
		vision = new Vision(sensor);
		mapDiscovered = new ArrayList<Point>();
	}

	private void updateMapDiscovered() {
		if (!this.mapDiscovered.contains(sensor.getPosicao()))
			this.mapDiscovered.add(sensor.getPosicao());
	}

	private void cognize() {
		vision.update();
		cognizeAvailableCell();
	}

	private void cognizeAvailableCell() {
		List<StateToMove> availables = new ArrayList<>();

		VisionEnum[] availablesStates = new VisionEnum[]{
				VisionEnum.COIN,
				VisionEnum.FLOOR
		};

		Point p = sensor.getPosicao();
		for (VisionEnum e : availablesStates) {
			if (vision.getTop() == e || canWalkToBank(e) || canWalkToPowerUp(e))
				availables.add(StateToMove.create(ActionEnum.UP, vision.getTop(), new Point(p.x, p.y + 1)));
			if (vision.getBottom() == e || canWalkToBank(e) || canWalkToPowerUp(e))
				availables.add(StateToMove.create(ActionEnum.DOWN, vision.getBottom(), new Point(p.x, p.y - 1)));
			if (vision.getLeft() == e || canWalkToBank(e) || canWalkToPowerUp(e))
				availables.add(StateToMove.create(ActionEnum.LEFT, vision.getLeft(), new Point(p.x - 1, p.y)));
			if (vision.getRight() == e || canWalkToBank(e) || canWalkToPowerUp(e))
				availables.add(StateToMove.create(ActionEnum.RIGHT, vision.getRight(), new Point(p.x + 1, p.y)));
		}

		if (availables.isEmpty())
			availables.add(StateToMove.create(ActionEnum.STOP, VisionEnum.FLOOR, p));

		this.availableCells = availables;
	}

	private void searchForCoins() {
		List<ActionEnum> posibleActions = vision.getCoinCells();
		if (posibleActions.isEmpty())
			return;
		for (StateToMove available : availableCells)
			available.setHasCoins(posibleActions.contains(available.getAction()));
	}

	private void filterByHasCoins() {
		List<StateToMove> s = new ArrayList<StateToMove>();
		for (StateToMove x : availableCells) {
			if (x.hasCoins())
				s.add(x);
		}
		if (!s.isEmpty())
			availableCells = s;
	}

	private StateToMove choosePath() {
		if (sensor.getNumeroDeMoedas() >= GlobalVariables.GO_TO_THE_BANK){
			ActionEnum actionToBank = vision.getQuadrante(Constantes.posicaoBanco);
			return StateToMove.create(actionToBank, )
		}

		List<StateToMove> haveCoins = this.availableCells.stream().filter(x -> x.getVision() == VisionEnum.COIN || x.hasCoins()).collect(Collectors.toList());
		if (haveCoins.isEmpty()){

			List<StateToMove> toDiscover = this.availableCells.stream().filter(x -> !mapDiscovered.contains(x.getPosition())).collect(Collectors.toList());
			System.out.println("MapDiscovered: " + mapDiscovered.toString());
			System.out.println("ToDiscover: " + toDiscover.toString());

			if (toDiscover.isEmpty())
				return this.availableCells.get((int) (Math.random() * availableCells.size()));

			return toDiscover.get((int) (Math.random() * toDiscover.size()));
		}
		return haveCoins.get((int) (Math.random() * haveCoins.size()));
	}



	private boolean canWalkToBank(VisionEnum e) { return e == VisionEnum.BANK && haveCashToDepositInBank(); }
	private boolean haveCashToDepositInBank() { return sensor.getNumeroDeMoedas() >= GlobalVariables.MIN_COIN_TO_DEPOSIT; }

	private boolean canWalkToPowerUp(VisionEnum e) { return e == VisionEnum.POWER_UP && haveCashToBuyPowerUp(); }
	private boolean haveCashToBuyPowerUp() { return sensor.getNumeroDeMoedas() >= GlobalVariables.POWER_UP_PRICE; }
}