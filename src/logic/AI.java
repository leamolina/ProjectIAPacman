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
	private static ArrayList<Position> path = new ArrayList<>();
	private static ArrayList<Integer> scores = new ArrayList<>();
	private static String bestMove;
	private final static int deepthMax = 4;

	/**
	 * Cette méthode va appeler l'algorithme de recherche AndSearch afin d'estimer quel mouvement est le plus rentable pour le pacman
	 * @param beliefState le beliefState atuel
	 * @return la meilleure action possible en prenant en compte toutes les informations sur l'environnement
	 */
	public static String findNextMove(BeliefState beliefState){

		//S'il y a un changement de score, on vide la liste du chemin
		if(!scores.isEmpty() && beliefState.getScore() != scores.get(scores.size()-1)){
			scores.clear();
			path.clear();
		}

		//A chaque fois qu'on choisit un BeliefState, on ajoute son score et la position du pacman aux listes scores et path
		scores.add(beliefState.getScore());
		path.add(beliefState.getPacmanPosition());

		moves.clear();

		andSearch(beliefState,deepthMax); //Appel de la fonction andSearch

		return AI.bestMove;
	}


	/**
	 *
	 * @param result
	 * @param deepth
	 * @return
	 */
	public static double orSearch(Result result, int deepth) { //Moyenne
        double somme = 0;

		//Si la profondeur est égale à 0, on n'appelle plus andSearch, on fait désormais appel à l'heuristique pour estimer le score potentiel de chaque action
        if(deepth == 0){
            double score;
			for(BeliefState beliefState : result.getBeliefStates()){
				//Si le beliefState est déjà présent dans la table de transposition, on n'a pas besoin de faire appel à l'heuristique; on renvoie directement le score qui est stocké
				if(transpositionTable.containsKey(beliefState)){
					score = transpositionTable.get(beliefState);
				}
				//Sinon, on fait appel à l'heuristique, et on ajout le beliefState et son score associé à la table de transposition
				else{
					score = getHeuristic(beliefState);
					transpositionTable.put(beliefState, score);
				}
				somme += score;
			}

        }
		//Sinon, on appelle encore andSearch
		else{
            double score;
			for(BeliefState beliefState : result.getBeliefStates()){
				if(transpositionTable.containsKey(beliefState)){
					score = transpositionTable.get(beliefState);
					//S'il y a un cycle, on ajoute un malus au score:
					if(containsCycle(beliefState.getPacmanPosition())){
						score-=1;
					}
				} else{
					score = andSearch(beliefState, deepth);
				}
				somme += score;
			}
        }
        return somme/result.getBeliefStates().size();

    }


	/**
	 *
	 * @param beliefState le BeliefState à étendre et à explorer
	 * @param deepth la profondeur de la recherche
	 * @return le score maximum que parmis les scores résultants des 4 actions possiblrs
	 */
	private static double andSearch(BeliefState beliefState, int deepth) { //Max
		Plans plan = beliefState.extendsBeliefState(); //On étend le beliefState
		double scoreMax = -1;

		for (int i = 0; i < plan.size(); i++) {

			Result result = plan.getResult(i);
			double score = orSearch(result, deepth-1); //On récupère le score moyen de tous les beliefState du result
			//On lui interdit les murs
			if(plan.getAction(i).size()>1){
				continue;
			}
			String move = plan.getAction(i).get(0); //On récupère l'action associée au score
			moves.put(score, move); //On ajoute le score et le mouvement associé dans une liste
			if(scoreMax < score){
				scoreMax = score; //Mise à jour du score maximum si on trouve un meilleur candidat
			}

		}
		bestMove = moves.get(scoreMax); //Le meilleur score (celui qui sera utilisé par FindNextMove) est le mouvement ayant le meilleur score
		return scoreMax; //On renvoie le meilleur score
	}

	/**
	 * @param beliefState un beliefState à considérer
	 * @return une estimation du score du beliefState en fonction de la distance qui sépare le Pacman des gommes restantes
	 */
	private static double getHeuristic(BeliefState beliefState) {
		int malus = 0;
		//Si ce beliefState mène à la mort du Pacman, on renvoie un score égal à 0
		if(beliefState.getLife()==0){
			return 0;
		}
		//Si le nombre de cycles detectés est supérieur à 3, un malus est ajouté
		else if(containsCycle(beliefState.getPacmanPosition())){
			malus+=1;
		}
		double bonus = 0;
		int lignePacman = beliefState.getPacmanPosition().getRow();
		int colonnePacman = beliefState.getPacmanPosition().getColumn();

		//Le faire aller vers la gomme la plus proche (celle dont la distance de Mannhatan est la plus petite)
		double minDistanceMannhatanGommes = 100000;
		for (int i=0; i<25; i++){ //Nombre de ligne
			for(int j=0; j<25; j++) { //Nombtr de colonnes
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


	/**
	 * @param pos la position du pacman dans le BeliefState à considérer
	 * @return true si le nombre de cycles detectés est supérieur à 3, false sinon
	 */
	public static boolean containsCycle(Position pos) {
		int cpt =0;
		//On commence à la fin car on sait que s'il y a un cycle, il est plus probable qu'il se trouve à la fin
		for (int i=path.size()-1; i>=0 ;i--) {
			Position p = path.get(i);
			//Si la position du pacman est la même qu'une des positions stockées dans path (et que le score n'a pas changé), cela signifie qu'il est revenu à un endroit déjà visité : c'est un cycle
			if (p.getRow() == pos.getRow() && p.getColumn() == pos.getColumn()) {
				cpt++; //Dès qu'un cycle est détécté, le compteur est incrémenté
				if(cpt>=3){
					return true;
				}
			}
		}

		return false;

	}
}