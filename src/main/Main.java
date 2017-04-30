/*javapokertexasholdem is an Open Source project licensed under the terms of
the LGPLv3 license.  Please see <http://www.gnu.org/licenses/lgpl-3.0.html>
for license text.
*/

package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import main.GameTexasHoldem.StateTuple;

public class Main {
	
	static double [][][][][] qValues = new double [10][13][4][4][3];
	static double learningRate = 0.2;
	static double gamma = 0.9;
	static double epsilon = 0.2;

	
	public static void main(String[] args) throws IOException {

		for (int i = 0; i < qValues.length; i ++) {
			for (int j = 0; j < qValues[i].length; j ++) {
				for (int k = 0; k < qValues[i][j].length; k ++) {
					for (int l = 0; l < qValues[i][j][k].length; l ++) {
						for (int m = 0; m < qValues[i][j][k][l].length; m ++) {
							if (m == qValues[i][j][k][l].length - 1 && l == qValues[i][j][k].length - 1) {
								qValues[i][j][k][l][m] = Double.NEGATIVE_INFINITY;
							}
							else {
								if (m == 0) {
									qValues[i][j][k][l][m] = -4;
								}
								else {
									qValues[i][j][k][l][m] = 4;
								}
							}
						}
					}
				}
			}
		}
		
		
		
		
		Player player = new Player();
		player.AIID = 1;
		Player dealer = new Player();
		dealer.AIID = 0;
		String currDir = System.getProperty("user.dir");
		//getStatsFull
		System.out.println("Player start chips (the AI): " + player.getChips());
		System.out.println("Dealer start chips (the non-AI): " + dealer.getChips());
		
		String fullFileName = currDir + "/PokerStats" + 80000 + "-full.csv";
		getStatsFull(fullFileName, 80000, dealer, player);
		
		System.out.println("Player final chips (the AI): " + player.getChips());
		System.out.println("Dealer final chips (the non-AI): " + dealer.getChips());
		
		player.AIID = 1;
		player.totalWinnings = 0;
		player.setChips(200000);
		dealer.AIID = 2;
		dealer.totalWinnings = 0;
		dealer.setChips(200000);
		
		System.out.println("getStatsFull - OK - " + fullFileName);
		
		
		
		//getStatsFull(fullFileName, 5000, dealer, player);
		
		//System.out.println("Player final chips (the AI): " + player.getChips());
		//System.out.println("Dealer final chips (the non-AI): " + dealer.getChips());
		
		
		System.out.println("");
		System.out.println("Program completed.");
	}

	private static void getStatsFull(String path, int executions, Player dealer, Player player) throws IOException {
		BufferedWriter bwFull = new BufferedWriter(new FileWriter(path));
		
		int bigBlind = 4;
		bwFull.write("DEALER DEAL;PLAYER DEAL;DEALER CALL FLOP;PLAYER CALL FLOP;DEALER BET TURN;PLAYER BET TURN;DEALER BET RIVER;PLAYER BET RIVER;WINNER;DEALER CHIPS;PLAYER CHIPS\n");
		

		for (int i = 0; i < executions && dealer.getChips() > 0 && player.getChips() > 0; i++) {
			GameTexasHoldem game = new GameTexasHoldem();
			
//			if (i % 100 == 0) {
//				System.out.println(player.totalWinnings + "\t" + dealer.totalWinnings);
//			}
			
			
			player.setInvestedChips(0);
			dealer.setInvestedChips(0);
			
			String retLine;
			if (i % 2 == 0) {
				game.newGame(new Deck(), bigBlind, dealer, player);
				retLine = performOneRound(game, dealer, player, qValues);
				retLine = retLine + ";" + dealer.getChips() + ";" + player.getChips();
			}
			else {
				game.newGame(new Deck(), bigBlind, player, dealer);
				retLine = performOneRound(game, player, dealer, qValues);
				retLine = retLine + ";" + dealer.getChips() + ";" + player.getChips();
			}
			bwFull.write(retLine + "\n");
			
//			
//			System.out.println("Round " + i);
//			System.out.println("Pot: " + game.getPot());
//			System.out.println("Dealer: " + dealer.getChips());
//			System.out.println("Player: " + player.getChips());

			
		}
		bwFull.close();
	}
	
	private static String performOneRound(GameTexasHoldem game, Player dealer, Player player, double[][][][][] qValues) {
		ArrayList<StateTuple> stateList = new ArrayList<StateTuple>();
		
		game.deal();
		String retLine = new String(dealer.getRankingEnum().toString() + ";");
		retLine += player.getRankingEnum().toString() + ";";
		
		StateTuple preFlopState = game.bettingPreFlop(dealer, player);
		stateList.add(preFlopState);
		int preFlopResult = preFlopState.result;
		
		if (preFlopResult != 0) {
			retLine += "PreFlopEnd;";
			if (preFlopResult == 2) {
				dealer.addChips(game.getPot());
				dealer.setLastWinnings(game.getPot());
				updateQValues(player, dealer, stateList, qValues);
				retLine += "Dealer";
			}
			else {
				player.addChips(game.getPot());
				player.setLastWinnings(game.getPot());
				updateQValues(player, dealer, stateList, qValues);
				retLine += "Player";
			}
			player.setInvestedChips(0); player.setLastWinnings(0);
			dealer.setInvestedChips(0); dealer.setLastWinnings(0);
			return retLine;
		}
		
		game.callFlop();
		retLine += dealer.getRankingEnum().toString() + ";";
		retLine += player.getRankingEnum().toString() + ";";
		
		StateTuple flopState = game.bettingStandard(dealer, player, 1);
		stateList.add(flopState);
		int flopResult = flopState.result;
		
		if (flopResult != 0) {
			retLine += "FlopEnd;";
			if (flopResult == 2) {
				dealer.addChips(game.getPot());
				dealer.setLastWinnings(game.getPot());
				updateQValues(player, dealer, stateList, qValues);
				retLine += "Dealer";
			}
			else {
				player.addChips(game.getPot());
				player.setLastWinnings(game.getPot());
				updateQValues(player, dealer, stateList, qValues);
				retLine += "Player";
			}
			player.setInvestedChips(0); player.setLastWinnings(0);
			dealer.setInvestedChips(0); dealer.setLastWinnings(0);
			return retLine;
		}
		
		game.betTurn();
		retLine += dealer.getRankingEnum().toString() + ";";
		retLine += player.getRankingEnum().toString() + ";";
		
		StateTuple turnState = game.bettingStandard(dealer, player, 2);
		stateList.add(turnState);
		int turnResult = turnState.result;
		
		if (turnResult != 0) {
			retLine += "TurnEnd;";
			if (turnResult == 2) {
				dealer.addChips(game.getPot());
				dealer.setLastWinnings(game.getPot());
				updateQValues(player, dealer, stateList, qValues);
				retLine += "Dealer";
			}
			else {
				player.addChips(game.getPot());
				player.setLastWinnings(game.getPot());
				updateQValues(player, dealer, stateList, qValues);
				retLine += "Player";
			}
			player.setInvestedChips(0); player.setLastWinnings(0);
			dealer.setInvestedChips(0); dealer.setLastWinnings(0);
			return retLine;
		}
		
		game.betRiver();
		retLine += dealer.getRankingEnum().toString() + ";";
		retLine += player.getRankingEnum().toString() + ";";
		
		StateTuple riverState = game.bettingStandard(dealer, player, 3);
		stateList.add(riverState);
		int riverResult = riverState.result;
		
		if (riverResult != 0) {
			retLine += "RiverEnd;";
			if (riverResult == 2) {
				dealer.addChips(game.getPot());
				dealer.setLastWinnings(game.getPot());
				updateQValues(player, dealer, stateList, qValues);
				retLine += "Dealer";
			}
			else {
				player.addChips(game.getPot());
				player.setLastWinnings(game.getPot());
				updateQValues(player, dealer, stateList, qValues);
				retLine += "Player";
			}
			player.setInvestedChips(0); player.setLastWinnings(0);
			dealer.setInvestedChips(0); dealer.setLastWinnings(0);
			return retLine;
		}
		
		List<IPlayer> winnerList = game.getWinner();
		retLine += "FullEnd;";
		
		if (winnerList.size() == 1) {
			Player winner = (Player) winnerList.get(0);
			winner.addChips(game.getPot());
			winner.setLastWinnings(game.getPot());
			updateQValues(player, dealer, stateList, qValues);
			retLine += (winnerList.get(0).equals(dealer)) ? "Dealer" : "Player";
			//System.out.println((winnerList.get(0).equals(dealer)) ? "Dealer won - full" : "Player won- full");
			player.setInvestedChips(0); player.setLastWinnings(0);
			dealer.setInvestedChips(0); dealer.setLastWinnings(0);
		} 
		else {
			Player winner1 = (Player) winnerList.get(0);
			winner1.addChips(game.getPot() / 2);
			winner1.setLastWinnings(game.getPot() / 2);
			Player winner2 = (Player) winnerList.get(1);
			winner2.addChips(game.getPot() / 2);
			winner2.setLastWinnings(game.getPot() / 2);
			updateQValues(player, dealer, stateList, qValues);
			retLine += "Draw Game";
			//System.out.println("Draw");
			player.setInvestedChips(0); player.setLastWinnings(0);
			dealer.setInvestedChips(0); dealer.setLastWinnings(0);
		}
		
		
		return retLine;
	}
	
	public static void updateQValues (Player dealer, Player player, ArrayList<StateTuple> stateList, double[][][][][] qValues) {
		
		dealer.totalWinnings = dealer.totalWinnings + (dealer.getLastWinnings() - dealer.getInvestedChips());
		player.totalWinnings = player.totalWinnings + (player.getLastWinnings() - player.getInvestedChips());
		
		int reward;
		if (player.AIID == 1) {
			reward = player.getLastWinnings() - player.getInvestedChips();
		}
		else {
			reward = dealer.getLastWinnings() - dealer.getInvestedChips();
		}
		

		double weight = 1;
		for (int i = stateList.size() - 1; i >= 0; i --) {
			weight = (((double) i + 1) / 10) * 2;
			
			
			int hand = stateList.get(i).hand;
			int highCard = stateList.get(i).highCard;
			ArrayList<Integer> actionList = stateList.get(i).actions;
			for (int j = actionList.size() - 1; j >= 0; j --) {
				
				
				int actionChoice;
				if(actionList.get(j) != -1) {
					actionChoice = actionList.get(j);
				}
				else {
					continue;
				}
				
				double actionProb = qValues[hand][highCard][i][j][actionChoice];
				
				double bestProb = Double.NEGATIVE_INFINITY;
				
				for (int k = 0; k < 3; k ++) {
					double action = Main.qValues[hand][highCard][i][j][k];
					if (action > bestProb) {
						bestProb = action;
					}
				}
				
				double change = (reward + gamma * bestProb - actionProb) * weight;
				qValues[hand][highCard][i][j][actionChoice] = qValues[hand][highCard][i][j][actionChoice] + learningRate * change;
				
			}
			weight = weight - 0.2;
		}
		
		
	}
	
}
