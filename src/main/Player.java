package main;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public class Player implements IPlayer, Serializable {

	private static final long serialVersionUID = 4664480702994610549L;
	private Card[] cards = new Card[2];
	private RankingEnum rankingEnum = null;
	private List<Card> rankingList = null;
	private Card highCard = null;
	private int chips = 200000;
	private int lastBet = 0;
	private int investedChips = 0;
	public int lastWinnings = 0;
	public int totalWinnings = 0;
	public int AIID;
	
	public int decideBet(int minCan, int minMust, int stage, int subStage) {
		//Use player's high card, hand, stage and subStage to look up the static qvalue thing in the gamerunner.
		double prevAction = Double.NEGATIVE_INFINITY;
		int choice = 0;
		Random random = new Random();
		
		if(AIID == 0) {
			if (subStage < 3) {
				choice = (random.nextInt(2) + 1);
			}
			else {
				
				choice = 1;//random.nextInt(2);
			}
		}
		else {
			if (random.nextDouble() < Main.epsilon) {
				if (subStage < 3) {
					choice = (random.nextInt(2) + 1);
				}
				else {
					
					choice = 1;//random.nextInt(2);
				}
				
			}
			else {
				for (int i = 0; i < 3; i ++) {
					double action = Main.qValues[RankingUtil.getRankingToInt(this)][highCard.getRankToInt()][stage][subStage][i];
					if (action > prevAction) {
						prevAction = action;
						choice = i;
					}
				}
			}
		}
		
		if (choice == 1) {
			if (minMust > this.chips) {
				this.lastBet = this.chips;
				this.investedChips = this.investedChips + this.lastBet;
				setChips(0);
			}
			else {
				this.lastBet = minMust;
				this.investedChips = this.investedChips + this.lastBet;
				addChips(-(this.lastBet));
			}
			return choice;
		}
		
		else if (choice == 2) {
			
			//int betAmount = (int) ((random.nextDouble() * 0.1) * (this.chips - minCan));
			if (minCan > this.chips) {
				this.lastBet = this.chips;
				this.investedChips = this.investedChips + this.lastBet;
				setChips(0);
			}
			else {
				this.lastBet = minCan;// + betAmount;
				this.investedChips = this.investedChips + this.lastBet;
				addChips(-(this.lastBet));
			}
			
			
			return choice;
		}
		this.lastBet = 0;
		return choice;
	}
	
	public void makeBlinds(int blindAmount) {
		if (this.chips < blindAmount) {
			this.lastBet = this.chips;
			this.investedChips = this.investedChips + this.lastBet;
			setChips(0);
		}
		else {
			this.lastBet = blindAmount;
			this.investedChips = this.investedChips + this.lastBet;
			addChips(-(this.lastBet));
		}
	}
	
	public Card getHighCard() {
		return highCard;
	}

	public void setHighCard(Card highCard) {
		this.highCard = highCard;
	}

	public RankingEnum getRankingEnum() {
		return rankingEnum;
	}

	public void setRankingEnum(RankingEnum rankingEnum) {
		this.rankingEnum = rankingEnum;
	}

	public List<Card> getRankingList() {
		return rankingList;
	}

	public void setRankingList(List<Card> rankingList) {
		this.rankingList = rankingList;
	}

	public Card[] getCards() {
		return cards;
	}

	public void setCards(Card[] cards) {
		this.cards = cards;
	}
	
	public int getChips() {
		return chips;
	}
	
	public void addChips(int amount) {
		this.chips = this.chips + amount;
	}
	
	public void setChips(int amount) {
		this.chips = amount;
	}
	
	public int getLastBet() {
		return lastBet;
	}
	
	public void setLastBet(int lastBet) {
		this.lastBet = lastBet;
	}
	
	public int getInvestedChips() {
		return this.investedChips;
	}
	
	public void setInvestedChips(int investedChips) {
		this.investedChips = investedChips;
	}
	
	public int getLastWinnings() {
		return this.lastWinnings;
	}
	
	public void setLastWinnings(int winnings) {
		this.lastWinnings = winnings;
	}
	
}
