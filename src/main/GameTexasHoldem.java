package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameTexasHoldem implements Serializable {

	private static final long serialVersionUID = 967261359515323981L;
	private Deck deck;
	private List<IPlayer> players;
	private List<Card> tableCards;
	
	private int pot;
	private int bigBlind;
	private int minMust;
	private int minCan;
	

	public void newGame(Deck deck, int bigBlind, IPlayer player1, IPlayer... _players) {
		this.deck = deck;
		tableCards = new ArrayList<Card>();
		players = new ArrayList<IPlayer>();
		//the game needs at least one player
		players.add(player1);
		players.addAll(Arrays.asList(_players));
		this.pot = 0;
		this.bigBlind = bigBlind;
		this.minMust = 0;
		this.minCan = bigBlind;
	}

	//To abandon the game
	public void removePlayer(IPlayer player) {
		players.remove(player);
	}
	
	public StateTuple bettingStandard(Player dealer, Player player, int stage) {
		
		int minBet;
		this.minMust = 0;
		if (stage > 1) {
			minBet = bigBlind * 2;
			this.minCan = bigBlind * 2;
		}
		else {
			minBet = bigBlind;
			this.minCan = bigBlind;
		}
		
		ArrayList<Integer> actionList = new ArrayList<Integer>();
		for (int i = 0; i < 4; i ++) {
			actionList.add(-1);
		}
		int hand;
		int highCard;
		if (dealer.AIID == 1) {
			hand = RankingUtil.getRankingToInt(dealer);
			highCard = dealer.getHighCard().getRankToInt();
		}
		else {
			hand = RankingUtil.getRankingToInt(player);
			highCard = player.getHighCard().getRankToInt();
		}
		
		
		
		if (player.getChips() > 0 && dealer.getChips() > 0) {
			//state lookup 1
			int action = bettingAction(player, dealer, minBet, stage, 0);
			if (player.AIID == 1) actionList.set(0, action);
			if (action == 0) {
				dealer.setLastBet(0);
				player.setLastBet(0);
				
				int result = 2;
				StateTuple toReturn = new StateTuple(result, actionList, hand, highCard);
				return toReturn; //Dealer has won.
			}
			//state lookup 2
			action = bettingAction(dealer, player, minBet, stage, 1);
			if (dealer.AIID == 1) actionList.set(1, action);
			if (action == 0) {
				player.setLastBet(0);
				dealer.setLastBet(0);
				
				int result = 1;
				StateTuple toReturn = new StateTuple(result, actionList, hand, highCard);
				return toReturn; //Player has won.
			}
		}

		if (player.getInvestedChips() != dealer.getInvestedChips()) {
			if (player.getChips() > 0 && ((player.getInvestedChips() < dealer.getInvestedChips() && player.getChips() == 0) == false)) {
				//state lookup 3
				int action = bettingAction(player, dealer, minBet, stage, 2);
				if (player.AIID == 1) actionList.set(2, action);
				if (action == 0) {
					dealer.setLastBet(0);
					player.setLastBet(0);
					
					int result = 2;
					StateTuple toReturn = new StateTuple(result, actionList, hand, highCard);
					return toReturn; //Dealer has won.
				}
			}
			
			if (player.getInvestedChips() != dealer.getInvestedChips() && dealer.getChips() > 0 && ((dealer.getInvestedChips() < player.getInvestedChips() && dealer.getChips() == 0) == false)) {
				//state lookup 4
				int action = bettingAction(dealer, player, minBet, stage, 3);
				if (dealer.AIID == 1) actionList.set(3, action);
				if (action == 0) {
					player.setLastBet(0);
					dealer.setLastBet(0);
					
					int result = 1;
					StateTuple toReturn = new StateTuple(result, actionList, hand, highCard);
					return toReturn; //Player has won.
				}
			}
		}

		
		dealer.setLastBet(0);
		player.setLastBet(0);
		
		return new StateTuple(0, actionList, hand, highCard);
	}
	
	public StateTuple bettingPreFlop(Player dealer, Player player) {
		
		dealer.makeBlinds(bigBlind / 2);
		this.pot = pot + dealer.getLastBet();
		player.makeBlinds(bigBlind);
		this.pot = pot + player.getLastBet();
		
		minMust = bigBlind / 2;
		minCan = minMust + player.getLastBet();
		
		ArrayList<Integer> actionList = new ArrayList<Integer>();
		for (int i = 0; i < 4; i ++) {
			actionList.add(-1);
		}
		int hand;
		int highCard;
		if (dealer.AIID == 1) {
			hand = RankingUtil.getRankingToInt(dealer);
			highCard = dealer.getHighCard().getRankToInt();
		}
		else {
			hand = RankingUtil.getRankingToInt(player);
			highCard = player.getHighCard().getRankToInt();
		}
		
		if (player.getChips() > 0 && dealer.getChips() > 0) {
			//state lookup 1
			int action = bettingAction(dealer, player, bigBlind, 0, 0);
			if (dealer.AIID == 1) actionList.set(0, action);
			if (action == 0) {
				player.setLastBet(0);
				dealer.setLastBet(0);
				
				int result = 1;
				StateTuple toReturn = new StateTuple(result, actionList, hand, highCard);
				
				return toReturn; //Player has won.
			}
			//state lookup 2
			action = bettingAction(player, dealer, bigBlind, 0, 1);
			if (player.AIID == 1) actionList.set(1, action);
			if (action == 0) {
				dealer.setLastBet(0);
				player.setLastBet(0);
				
				int result = 2;
				StateTuple toReturn = new StateTuple(result, actionList, hand, highCard);
				
				return toReturn; //Dealer has won.
			}
		}
		
		if (player.getInvestedChips() != dealer.getInvestedChips()) {
			
			if (dealer.getChips() > 0 && ((player.getInvestedChips() < dealer.getInvestedChips() && player.getChips() == 0) == false)) {
				//state lookup 3
				int action = bettingAction(dealer, player, bigBlind, 0, 2);
				if (dealer.AIID == 1) actionList.set(2, action);
				if (action == 0) {
					player.setLastBet(0);
					dealer.setLastBet(0);
					
					int result = 1;
					StateTuple toReturn = new StateTuple(result, actionList, hand, highCard);
					
					return toReturn; //Player has won.
				}
			}
			
			if (player.getInvestedChips() != dealer.getInvestedChips() && player.getChips() > 0 && ((dealer.getInvestedChips() < player.getInvestedChips() && dealer.getChips() == 0) == false)) {
				//state lookup 4
				int action = bettingAction(player, dealer, bigBlind, 0, 3);
				if (player.AIID == 1) actionList.set(3, action);
				if (action == 0) {
					dealer.setLastBet(0);
					player.setLastBet(0);
					
					int result = 2;
					StateTuple toReturn = new StateTuple(result, actionList, hand, highCard);
					
					return toReturn; //Dealer has won.
				}
			}
			
		}
		dealer.setLastBet(0);
		player.setLastBet(0);
		
		return new StateTuple(0, actionList, hand, highCard);
	}
	
	private int bettingAction(Player toBet, Player other, int minBet, int stage, int subStage) {
		int action = toBet.decideBet(this.minCan, this.minMust, stage, subStage);
		this.pot = this.pot + toBet.getLastBet();
		minMust = Math.abs(toBet.getInvestedChips() - other.getInvestedChips());
		minCan = minBet;
		if (minMust > 0) {
			minCan = minMust + minBet;
		}
		return action;
	}

	public void deal() {
		for (IPlayer player : players) {
			player.getCards()[0] = deck.pop();
			player.getCards()[1] = deck.pop();
		}
		checkPlayersRanking();
	}

	/**
	 * doble initial bet
	 */
	public void callFlop() {
		deck.pop();
		tableCards.add(deck.pop());
		tableCards.add(deck.pop());
		tableCards.add(deck.pop());
		checkPlayersRanking();
	}

	public void betTurn() {
		deck.pop();
		tableCards.add(deck.pop());
		checkPlayersRanking();
	}

	public void betRiver() {
		deck.pop();
		tableCards.add(deck.pop());
		checkPlayersRanking();
	}

	public List<IPlayer> getWinner() {
		checkPlayersRanking();
		List<IPlayer> winnerList = new ArrayList<IPlayer>();
		IPlayer winner = players.get(0);
		Integer winnerRank = RankingUtil.getRankingToInt(winner);
		winnerList.add(winner);
		for (int i = 1; i < players.size(); i++) {
			IPlayer player = players.get(i);
			Integer playerRank = RankingUtil.getRankingToInt(player);
			//Draw game
			if (winnerRank == playerRank) {
				IPlayer highHandPlayer = checkHighSequence(winner, player);
				//Draw checkHighSequence
				if (highHandPlayer == null) {
					highHandPlayer = checkHighCardWinner(winner, player);
				}
				//Not draw in checkHighSequence or checkHighCardWinner
				if (highHandPlayer != null && !winner.equals(highHandPlayer)) {
					winner = highHandPlayer;
					winnerList.clear();
					winnerList.add(winner);
				} else if (highHandPlayer == null) {
					//Draw in checkHighSequence and checkHighCardWinner
					winnerList.add(winner);
				}
			}
			else if (winnerRank < playerRank) {
				winner = player;
				winnerList.clear();
				winnerList.add(winner);
			}
			winnerRank = RankingUtil.getRankingToInt(winner);
		}

		return winnerList;
	}

	private IPlayer checkHighSequence(IPlayer player1, IPlayer player2) {
		Integer player1Rank = sumRankingList(player1);
		Integer player2Rank = sumRankingList(player2);
		if (player1Rank > player2Rank) {
			return player1;
		} else if (player1Rank < player2Rank) {
			return player2;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private IPlayer checkHighCardWinner(IPlayer player1, IPlayer player2) {
		IPlayer winner = compareHighCard(player1, player1.getHighCard(),
				player2, player2.getHighCard());
		if (winner == null) {
			Card player1Card = RankingUtil.getHighCard(player1,
					Collections.EMPTY_LIST);
			Card player2Card = RankingUtil.getHighCard(player2,
					Collections.EMPTY_LIST);
			winner = compareHighCard(player1, player1Card, player2, player2Card);
			if (winner != null) {
				player1.setHighCard(player1Card);
				player2.setHighCard(player2Card);
			} else if (winner == null) {
				player1Card = getSecondHighCard(player1, player1Card);
				player2Card = getSecondHighCard(player2, player2Card);
				winner = compareHighCard(player1, player1Card, player2,
						player2Card);
				if (winner != null) {
					player1.setHighCard(player1Card);
					player2.setHighCard(player2Card);
				}
			}
		}
		return winner;
	}

	private IPlayer compareHighCard(IPlayer player1, Card player1HighCard,
			IPlayer player2, Card player2HighCard) {
		if (player1HighCard.getRankToInt() > player2HighCard.getRankToInt()) {
			return player1;
		} else if (player1HighCard.getRankToInt() < player2HighCard
				.getRankToInt()) {
			return player2;
		}
		return null;
	}

	/*
	 * TODO This method must be moved to RankingUtil
	 */
	private Card getSecondHighCard(IPlayer player, Card card) {
		if (player.getCards()[0].equals(card)) {
			return player.getCards()[1];
		}
		return player.getCards()[0];
	}

	public List<Card> getTableCards() {
		return tableCards;
	}
	
	public int getPot() {
		return pot;
	}

	/*
	 * TODO This method must be moved to RankingUtil
	 */
	private Integer sumRankingList(IPlayer player) {
		Integer sum = 0;
		for (Card card : player.getRankingList()) {
			sum += card.getRankToInt();
		}
		return sum;
	}

	private void checkPlayersRanking() {
		for (IPlayer player : players) {
			RankingUtil.checkRanking(player, tableCards);
		}
	}
	public class StateTuple {
		public int result;
		public ArrayList<Integer> actions;
		public int hand;
		public int highCard;
		
		public StateTuple(int result, ArrayList<Integer> actions, int hand, int highCard) {
			this.result = result;
			this.actions = actions;
			this.hand = hand;
			this.highCard = highCard;
		}
		
	}
	
}
