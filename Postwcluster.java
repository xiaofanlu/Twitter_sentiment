import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;


public class Postwcluster {
	static HashMap<String, String> word2path = new HashMap<String, String>();
	static HashMap<String, String> path2word = new HashMap<String, String>();
	
	public static void main(String[] args) {
		try {
			String wclusterpath = "/u/ywu/nlp/final/brown-cluster/word_cluster-c1000-p1.out/paths";
			wordClusters(wclusterpath);
			String inputpath = "/u/ywu/nlp/final/trainingandtestdata/training.1600000.processed.noemoticon.csv";
			String pospath = "/u/ywu/nlp/final/trainingandtestdata/pos_train.txt";
			String netpath = "/u/ywu/nlp/final/trainingandtestdata/net_train.txt";
			String negpath = "/u/ywu/nlp/final/trainingandtestdata/neg_train.txt";
			splitFiles(inputpath, pospath, netpath, negpath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void wordClusters(String inputpath) throws IOException{
		
		HashMap<String, Integer> path2freq = new HashMap<String, Integer>();
		File input = new File(inputpath);
		FileReader reader = new FileReader(input);
		BufferedReader br = new BufferedReader(reader);
		
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] tmp = line.split("\\t");
			String path = tmp[0];
			String word = tmp[1];
			int freq = Integer.parseInt(tmp[2].trim());
			if (tmp.length < 3)
				continue;
			word2path.put(word, path);
			if(path2freq.get(path) == null || path2freq.get(path) < freq){
				path2freq.put(path, freq);
				path2word.put(path, word);
			}
		}
		
		br.close();
	}
	
	//split tweets with different sentiment to seperate files
	//for sentiment treebank training
	//tentative to change
	private static void splitFiles(String inputpath, String pospath,
			String netpath, String negpath) throws IOException {
		File input = new File(inputpath);
		File posfile = new File(pospath);
		File netfile = new File(netpath);
		File negfile = new File(negpath);

		FileReader reader = new FileReader(input);
		BufferedReader br = new BufferedReader(reader);

		FileWriter pos = new FileWriter(posfile);
		FileWriter net = new FileWriter(netfile);
		FileWriter neg = new FileWriter(negfile);

		int posnum = 0;
		int netnum = 0;
		int negnum = 0;

		System.out.println("Preprocess for sentiment training");

		String line = null;
		while ((line = br.readLine()) != null) {
			String[] tmp = line.split(",");
			if (tmp.length < 6)
				continue;
			int i = line.indexOf(tmp[5].trim());
			String tweet = line.substring(i+1, line.length() - 1);
			tweet = Preprocess.filter(tweet);
			tweet = clustering(tweet);
			char c = tmp[0].charAt(1);
			switch (c) {
			case '0':
				neg.write(c + ": "+ tweet+"\n");
				negnum++;
				break;
			case '2':
				net.write(c + ": " + tweet+"\n");
				netnum++;
				break;
			case '4':
				pos.write(c + ": " + tweet+"\n");
				posnum++;
				break;
			}
		}

		System.out
				.println("Preprocessed " + negnum + " negative tweets, "
						+ netnum + " neutral tweets and " + posnum
						+ " positive tweets");
		br.close();
		pos.close();
		net.close();
		neg.close();
	}
	
	public static String clustering(String s){
		StringBuffer buffer = new StringBuffer();
		
		String[] tmp = s.split(" ");
		
		for(int i = 0; i < tmp.length; i++){			
			String word = null;
			//TODO realize fuzzword
			//if (word2path.get(tmp[i]) == null) tmp[i] = fuzzword(tmp[i]);
			word = path2word.get(word2path.get(tmp[i]));
			buffer.append(word);
		}
		
		return buffer.toString();
	}
}
