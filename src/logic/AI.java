package logic;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

import view.Gomme;


/**
 * class used to represent plan. It will provide for a given set of results an action to perform in each result
 */
class Plans{
	ArrayList<Result> results;
	ArrayList<ArrayList<String>> actions;
	
	/**
	 * construct an empty plan
	 */
	public Plans() {
		this.results = new ArrayList<Result>();
		this.actions = new ArrayList<ArrayList<String>>();
	}
	
	/**
	 * add a new pair of belief-state and corresponding (equivalent) actions 
	 * @param beliefBeliefState the belief state to add
	 * @param action a list of alternative actions to perform. Only one of them is chosen but their results should be similar
	 */
	public void addPlan(Result beliefBeliefState, ArrayList<String> action) {
		this.results.add(beliefBeliefState);
		this.actions.add(action);
	}
	
	/**
	 * return the number of belief-states/actions pairs
	 * @return the number of belief-states/actions pairs
	 */
	public int size() {
		return this.results.size();
	}
	
	/**
	 * return one of the belief-state of the plan
	 * @param index index of the belief-state
	 * @return the belief-state corresponding to the index
	 */
	public Result getResult(int index) {
		return this.results.get(index);
	}
	
	/**
	 * return the list of actions performed for a given belief-state
	 * @param index index of the belief-state
	 * @return the set of actions to perform for the belief-state corresponding to the index
	 */
	public ArrayList<String> getAction(int index){
		return this.actions.get(index);
	}
}

/**
 * class used to represent a transition function i.e., a set of possible belief states the agent may be in after performing an action
 */
class Result{
	private ArrayList<BeliefState> beliefStates;

	/**
	 * construct a new result
	 * @param states the set of states corresponding to the new belief state
	 */
	public Result(ArrayList<BeliefState> states) {
		this.beliefStates = states;
	}

	/**
	 * returns the number of belief states
	 * @return the number of belief states
	 */
	public int size() {
		return this.beliefStates.size();
	}

	/**
	 * return one of the belief state
	 * @param index the index of the belief state to return
	 * @return the belief state to return
	 */
	public BeliefState getBeliefState(int index) {
		return this.beliefStates.get(index);
	}
	
	/**
	 * return the list of belief-states
	 * @return the list of belief-states
	 */
	public ArrayList<BeliefState> getBeliefStates(){
		return this.beliefStates;
	}
}


/**
 * class implement the AI to choose the next move of the Pacman
 */
public class AI{
	/**
	 * function that compute the next action to do (among UP, DOWN, LEFT, RIGHT)
	 * @param beliefState the current belief-state of the agent
	 * @return a string describing the next action (among PacManLauncher.UP/DOWN/LEFT/RIGHT)
	 */
	public static String findNextMove(BeliefState beliefState) {
		int deepth = 3;
		ArrayList<String> bestMoves = new ArrayList<>();
		Plans plan = beliefState.extendsBeliefState();
		double maxPotentialScore = Double.NEGATIVE_INFINITY;
		for(int i=0; i<plan.size(); i++) {
			Result result = plan.getResult(i);
			double potentialScore = getPotentialScore(result,deepth);
			if(potentialScore>maxPotentialScore){
				maxPotentialScore = potentialScore;
				bestMoves.clear();
				for(int j=0; j<plan.getAction(i).size(); j++){
					bestMoves.add(plan.getAction(i).get(j));
				}
			}
			else if(potentialScore==maxPotentialScore){
				for(int j=0; j<plan.getAction(i).size(); j++){
					bestMoves.add(plan.getAction(i).get(j));
				}
			}
		}
		//On choisit le max de toutes les moyennes choisies
		if(bestMoves.size()>0){
			Random random = new Random();
			int randomNumber = random.nextInt(bestMoves.size());
			return bestMoves.get(randomNumber);
		}
		else{
			//Pour la dernière itération (avant qu'il se fasse manger, y'a aucun bon score donc listBestMove sera vide)
			return PacManLauncher.LEFT;
		}	}

	private static double getPotentialScore(Result result, int deepth) {
		//Si on a terminé le jeu (qu'on a atteint un état final)
		if(isFinalNextMap(result)){
			return 1000000+1;
		}
		/*else if(isFinalGameOver(result)){
			return -100000;
		}*/
		//Si on a terminé de parcourir
		else if(deepth==0){
			double average=0;
			for(BeliefState beliefState: result.getBeliefStates()){
				average+=getHeuristic(beliefState);
			}
			return average/result.getBeliefStates().size();
		}
		else{
			double sumScore = 0;
			for(BeliefState beliefState: result.getBeliefStates()){
				Plans plan = beliefState.extendsBeliefState();
				double scoreMax = Double.NEGATIVE_INFINITY;
				for(int i=0; i<plan.size(); i++) {
					Result res = plan.getResult(i);
					double score = getPotentialScore(res, deepth-1);
					if(score>scoreMax){
						scoreMax = score;
					}
				}
				sumScore+=scoreMax;
			}
			double averageScore = sumScore/result.getBeliefStates().size();
			return averageScore;
		}

	}

	private static boolean isFinalNextMap(Result result) {
		for(BeliefState beliefState: result.getBeliefStates()){
			if(beliefState.getNbrOfGommes()==0){
				return true;
			}
		}
		return false;
	}

	private static boolean isFinalGameOver(Result result) {
		for(BeliefState beliefState: result.getBeliefStates()){
			if(beliefState.getLife()==0){
				return true;
			}
		}
		return false;
	}

	private static boolean isFinal(Result result) {
		for(BeliefState beliefState: result.getBeliefStates()){
			if(beliefState.getLife()==0 || beliefState.getNbrOfGommes()==0){
				return true;
			}
		}
		return false;
	}


	private static double getHeuristic(BeliefState beliefState) {
		if(beliefState.getLife()==0){return 0;}


		double bonus = 0;
		int lignePacman = beliefState.getPacmanPosition().getRow();
		int colonnePacman = beliefState.getPacmanPosition().getColumn();
		//char dirPacman = beliefState.getPacmanPosition().getDirection();

		//Le faire aller vers la gomme la plus proche
		double minDistanceMannhatanGommes = 100000;
		for (int i=0; i<20 ; i++){ //nb de ligne
			for(int j=0; j<19 ; j++) { //nb de colonnes
				if (beliefState.getMap(i, j) == '.') {
					double distanceMannhatanGommes = Math.abs(lignePacman - i) + Math.abs(colonnePacman - j);
					if(distanceMannhatanGommes < minDistanceMannhatanGommes){
						minDistanceMannhatanGommes = distanceMannhatanGommes;
					}
				}
			}
		}
		if(minDistanceMannhatanGommes!=0){
			bonus = 1/minDistanceMannhatanGommes;
		}
		return beliefState.getScore() + bonus;
	}


	//Cas où lignes ==
	private static boolean isWallRowBetween(Position pGhost, BeliefState beliefState) {
		int colonnePacman = beliefState.getPacmanPosition().getColumn();
		int lignePacman = beliefState.getPacmanPosition().getRow();
		int colonneGhost = pGhost.getColumn();
		int ligneGhost = pGhost.getRow();

		//On va parcourir les colonnes x (pour la ligne y) et vérifier qu'il n'y a pas de mur qui sépare le pacman du ghost
		int colonneStart = Math.min(colonnePacman, colonneGhost);
		int colonneEnd = Math.max(colonnePacman, colonneGhost);
		for (int colonne = colonneStart; colonne<colonneEnd; colonne++){
			if(beliefState.getMap(lignePacman, colonne) == '#'){
				return true;
			}
		}
		return false;

	}

	//cas column ==
	private static boolean isWallColBetween(Position pGhost, BeliefState beliefState) {
		int colonnePacman = beliefState.getPacmanPosition().getColumn();
		int lignePacman = beliefState.getPacmanPosition().getRow();
		int colonneGhost = pGhost.getColumn();
		int ligneGhost = pGhost.getRow();

		//On va parcourir les lignes y (pour la colonne x) et vérifier qu'il n'y a pas de mur qui sépare le pacman du ghost
		int ligneStart = Math.min(lignePacman, ligneGhost);
		int ligneEnd = Math.max(lignePacman, ligneGhost);
		for (int ligne = ligneStart; ligne<ligneEnd; ligne++){
			if(beliefState.getMap(ligne, colonnePacman) == '#'){
				return true;
			}
		}
		return false;

	}
}