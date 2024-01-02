package logic;
import java.util.*;
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
class BeliefStateSet{

	TreeMap<String, Double> set;
	public BeliefStateSet() {
		this.set = new TreeMap<String,Double>();

	}
	public void add(BeliefState beliefState, double value) {
		this.set.put(beliefState.toString(),value);

	}
	public Double getValue(BeliefState beliefState) {
		return this.set.get(beliefState.toString());

	}
	public int size() {
		return this.set.size();

	}

}
/**
 * class implement the AI to choose the next move of the Pacman
 */
public class AI{
	/**
	 * function that compute the next action to do (among UP, DOWN, LEFT, RIGHT)
	 * @param beliefState the current belief-state of the agent
	 * @param deepth the deepth of the search (size of the largest sequence of action checked)
	 * @return a string describing the next action (among PacManLauncher.UP/DOWN/LEFT/RIGHT)
	 */
	private static TreeMap<BeliefState, Double> transpositionTable = new TreeMap<>();
	private static HashMap<Double, String> moves = new HashMap<>();
	private static HashMap<Double, List<String>> movesBis = new HashMap<>();
	private static ArrayList<Position> path = new ArrayList<>();
	private static ArrayList<Integer> scores = new ArrayList<>();
	private static String bestMove;
	private final static int deepthMax = 4;
	public static String findNextMove(BeliefState beliefState){
		//S'il y a un changement de score, on vide la liste du chemin
		if(scores.size()>0 && beliefState.getScore() != scores.get(scores.size()-1)){
			scores.clear();
			path.clear();
		}
		scores.add(beliefState.getScore());
		path.add(beliefState.getPacmanPosition());

		//movesBis.clear();
		moves.clear();
		double bestScore = andSearch(beliefState,deepthMax);
		System.out.println("Le move qu'on a FINALEMENT choisi est " + AI.bestMove + "car son score est " + bestScore);

		//System.out.println("Cycle ? " + containsCycle(scores, path));
		return AI.bestMove;
	}


	public static boolean containsCycle(ArrayList<Integer> scores, ArrayList<Position> path, Position pos) {
		int cpt =0;
		//On commence à la fin car on sait que s'il y a un cycle, il est plus probable qu'il se trouve à la fin
		for (int i=path.size()-1; i>=0 ;i--) {
			Position p = path.get(i);
				if (p.getRow() == pos.getRow() && p.getColumn() == pos.getColumn()) {
					cpt++;
					if(cpt>=3){
						System.out.println("Ya un CYCLE");
						return true;
					}
				}
		}

		return false;
	}



	public static double orSearch(Result result, int deepth) { //Moyenne
		if(deepth == 0){
			double somme = 0;
			double score;
			for(BeliefState beliefState : result.getBeliefStates()){
				if(transpositionTable.containsKey(beliefState)){
					score = transpositionTable.get(beliefState);

				} else{
					score = getHeuristic(beliefState);
					transpositionTable.put(beliefState, score);

				}

				somme += score;
			}
			return somme/result.getBeliefStates().size();

		} else{
			double somme = 0;
			double score;
			for(BeliefState beliefState : result.getBeliefStates()){
				if(transpositionTable.containsKey(beliefState)){
					score = transpositionTable.get(beliefState);

				} else{
					score = andSearch(beliefState, deepth);
				}

				somme += score;
			}
			return somme/result.getBeliefStates().size();

		}

	}



	private static double andSearch(BeliefState beliefState, int deepth) { //Max
		Plans plan = beliefState.extendsBeliefState();
		double scoreMax = -1;
		for (int i = 0; i < plan.size(); i++) {
			Result result = plan.getResult(i);
			double score = orSearch(result, deepth-1);
			//Lui interdire les murs
			if(plan.getAction(i).size()>1){
				continue;
			}
			String move = plan.getAction(i).get(0);
			moves.put(score, move);
			if(deepth == deepthMax){
				System.out.println("on a l action " + move + " et son score est " + score);
			}
			if(scoreMax < score){
				scoreMax = score;
			}
		}
		bestMove = moves.get(scoreMax);
		if(deepth == deepthMax){
			System.out.println("Voici les choix qu'on an : " + moves);
		}
		return scoreMax;
	}


	private static double andSearchBis(BeliefState beliefState, int deepth) { //Max
		Plans plan = beliefState.extendsBeliefState();
		double scoreMax = -2;
		for (int i = 0; i < plan.size(); i++) {
			Result result = plan.getResult(i);
			double score = orSearch(result, deepth-1);

			//Lui interdire les murs
			if(plan.getAction(i).size()>1){
				continue;
			}

			String move = plan.getAction(i).get(0);
			if(movesBis.containsKey(score) && deepth == deepthMax){
				movesBis.get(score).add(move);
			}
			else if(deepth == deepthMax){
				List<String> movesList = new ArrayList<>();
				movesList.add(move);
				movesBis.put(score, movesList);
			}
			if(deepth == deepthMax){
				System.out.println("on a l action " + move + " et son score est " + score);
			}
			if(scoreMax <= score){
				scoreMax = score;
			}
		}
		if(deepth == deepthMax){
			List<String> bestMovesList = movesBis.get(scoreMax);
			if(!bestMovesList.isEmpty()){
				System.out.println("liste de choix : " + bestMovesList);
				Random random = new Random();
				int randomIndex = random.nextInt(bestMovesList.size());
				bestMove = bestMovesList.get(randomIndex);
				System.out.println("On a choisi l action " + bestMove + " dont le score est : " + scoreMax);
			}
			else{
				bestMove = PacManLauncher.LEFT;
			}
		}

		return scoreMax;
	}

	private static double getHeuristic(BeliefState beliefState) {
		int malus = 0;
		if(beliefState.getLife()==0){
			return 0;
		}
		else if(containsCycle(scores, path, beliefState.getPacmanPosition())){
			malus+=1;
		}
		double bonus = 0;
		int lignePacman = beliefState.getPacmanPosition().getRow();
		int colonnePacman = beliefState.getPacmanPosition().getColumn();

		//Le faire aller vers la gomme la plus proche
		double minDistanceMannhatanGommes = 100000;
		for (int i=0; i<25; i++){ //nb de ligne
			for(int j=0; j<25; j++) { //nb de colonnes
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

		return beliefState.getScore() + bonus - malus;

	}




}