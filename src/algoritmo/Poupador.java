package algoritmo;

import algoritmo.domain.*;
import controle.Constantes;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Poupador extends ProgramaPoupador {
	private boolean firstMove = true;
	private Vision vision;
	private List<StateToMove> availableCells;
	private List<VisionEnum> availablesStates = Arrays.asList(VisionEnum.COIN, VisionEnum.FLOOR);
	private StateToMove lastMove;

	public int acao() {
		System.out.println("-----------------------------------------------------------");


		initializeVariables();
		cognize();
		searchForCoins();

		System.out.println("Poupador posição: " + sensor.getPosicao().toString() + "(" + vision.positionUsedTimes(sensor.getPosicao()) + ")");
		System.out.println("Coins: " + sensor.getNumeroDeMoedas());
		System.out.println("Availables: " + availableCells.toString());
		System.out.println("Availables coins: " + filterByCoin().toString());
		System.out.println("Availables possible coins: " + filterByPossibelCoin().toString());

		lastMove = choosePath();

		System.out.println("Chose: " + lastMove.toString());

		System.out.println("-----------------------------------------------------------\n");
		return lastMove.getAction().getValue();
	}

	private void initializeVariables() {
		if (!firstMove)
			return;
		firstMove = false;
		vision = new Vision(sensor);
	}

	private void cognize() {
		vision.setSensor(sensor);
		vision.update();
		vision.updateMapDiscovered();
		cognizeAvailableCell();
	}

	private void cognizeAvailableCell() {
		List<StateToMove> availables = new ArrayList<>();

		Point p = sensor.getPosicao();
		for (VisionEnum e : availablesStates) {
			if (vision.getTop() == e || canWalkToBank(e) || canWalkToPowerUp(e))
				availables.add(StateToMove.create(ActionEnum.UP, vision.getTop(), vision.getTopPosition()));
			if (vision.getBottom() == e || canWalkToBank(e) || canWalkToPowerUp(e))
				availables.add(StateToMove.create(ActionEnum.DOWN, vision.getBottom(), vision.getBottomPosition()));
			if (vision.getLeft() == e || canWalkToBank(e) || canWalkToPowerUp(e))
				availables.add(StateToMove.create(ActionEnum.LEFT, vision.getLeft(), vision.getLeftPosition()));
			if (vision.getRight() == e || canWalkToBank(e) || canWalkToPowerUp(e))
				availables.add(StateToMove.create(ActionEnum.RIGHT, vision.getRight(), vision.getRightPosition()));
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

	private StateToMove choosePath() {
		StateToMove path = null;
		if (path == null)
			path = chooseBankPath();

		if (path == null)
			path = chooseCoinPath();

		if (path == null)
			path = choosePossibleCoinPath();

		if (path == null)
			return bestMove(availableCells);

		return path;
	}

	private StateToMove chooseCoinPath() {
		List<StateToMove> haveCoins = filterByCoin();
		if (haveCoins.isEmpty())
			return null;

		System.out.println("Coin path");
		return haveCoins.get(0);
	}

	private StateToMove choosePossibleCoinPath() {
		List<StateToMove> haveCoins = filterByPossibelCoin();
		if (haveCoins.isEmpty())
			return null;

		System.out.println("Possibel coin path");
		return bestMove(haveCoins);
	}

	private StateToMove chooseBankPath() {
		if (!haveToGoToTheBank())
			return null;
		List<ActionEnum> actionToBank = vision.getQuadrante(Constantes.posicaoBanco);
		if (actionToBank.isEmpty())
			return null;

		System.out.println("Bank path");

		List<StateToMove> x = convert(actionToBank);
		StateToMove best = bestMove(x);
		best = cutout(best);
		return best;
	}

	private List<StateToMove> convert(List<ActionEnum> e) {
		List<StateToMove> x = new ArrayList<StateToMove>();
		Point p = sensor.getPosicao();
		for (ActionEnum actionEnum: e) {
			switch (actionEnum) {
				case STOP:
					x.add(StateToMove.create(actionEnum, VisionEnum.FLOOR, new Point(p.x, p.y)));
					break;
				case UP:
					x.add(StateToMove.create(actionEnum, vision.getTop(), vision.getTopPosition()));
					break;
				case DOWN:
					x.add(StateToMove.create(actionEnum, vision.getBottom(), vision.getBottomPosition()));
					break;
				case RIGHT:
					x.add(StateToMove.create(actionEnum, vision.getRight(), vision.getRightPosition()));
					break;
				case LEFT:
					x.add(StateToMove.create(actionEnum, vision.getLeft(), vision.getLeftPosition()));
					break;
			}
		}

		return x;
	}

	private StateToMove bestMove(List<StateToMove> moves) {
		StateToMove best = null;

		for (StateToMove state: moves) {
			Point statePosition = state.getPosition();
			if (best == null)
				best = state;
			else {
				Point bestPosition = best.getPosition();
				if (vision.positionUsedTimes(statePosition) < vision.positionUsedTimes(bestPosition) ||
						(!availablesStates.contains(best.getVision()) && !canWalkToBank(best.getVision()) && !canWalkToPowerUp(best.getVision())))
					best = state;

			}
		}
		if (vision.positionUsedTimes(best.getPosition()) > 1)
			return availableCells.get(0);
			//return availableCells.get((int) (Math.random() * availableCells.size()));

		return best;
	}

	private StateToMove cutout(StateToMove path) {
		if (!availablesStates.contains(path.getVision()) && !canWalkToBank(path.getVision()) && !canWalkToPowerUp(path.getVision())) {
			List<StateToMove> stateToMove = availableCells.stream().filter(x -> x.getPosition() != path.getPosition()).collect(Collectors.toList());
			return stateToMove.get(0);
			//return stateToMove.get((int) Math.random() * stateToMove.size());
		}
		return path;
	}

	private boolean canWalkToBank(VisionEnum e) { return e == VisionEnum.BANK/* && haveCashToDepositInBank()*/; }
	private boolean haveCashToDepositInBank() { return sensor.getNumeroDeMoedas() >= GlobalVariables.MIN_COIN_TO_DEPOSIT; }
	private boolean haveToGoToTheBank() { return sensor.getNumeroDeMoedas() >= GlobalVariables.GO_TO_THE_BANK; }

	private boolean canWalkToPowerUp(VisionEnum e) { return e == VisionEnum.POWER_UP && haveCashToBuyPowerUp(); }
	private boolean haveCashToBuyPowerUp() { return sensor.getNumeroDeMoedas() >= GlobalVariables.POWER_UP_PRICE; }

	private List<StateToMove> filterByCoin() {
		return this.availableCells.stream().filter(x -> x.getVision() == VisionEnum.COIN).collect(Collectors.toList());
	}

	private List<StateToMove> filterByPossibelCoin() {
		return this.availableCells.stream().filter(x -> x.hasCoins()).collect(Collectors.toList());
	}
}