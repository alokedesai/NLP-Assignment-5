
public class WordWeight implements Comparable<WordWeight> {
	public Double weight;
	public String word;
	
	public WordWeight(String word, Double weight) {
		this.weight = weight;
		this.word = word;
	}
		
	public String toString() {
		return word + " => " + weight;
	}


	@Override
	public int compareTo(WordWeight o) {
		// TODO Auto-generated method stub
		return weight.compareTo(o.weight);
	}
}
