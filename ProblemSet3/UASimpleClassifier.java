import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class UASimpleClassifier {

	/********************************
	 * Name: Noah Buchanan 
	 * Username: ua100 
	 * Problem Set: PS3 
	 * Due Date: Feb 15, 2021
	 ********************************/

	HashMap<String, HashMap<Integer, ArrayList<String>>> data = new HashMap<>();
	HashMap<String, ArrayList<Integer>> classCount = new HashMap<>();
	ArrayList<String> classKeys = new ArrayList<String>();

	public static void main(String[] args) {

		UASimpleClassifier classify = new UASimpleClassifier();
		classify.train("train.txt");
		classify.test("test.txt");
	}

	public void train(String filename) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));

			String line = br.readLine();
			String[] split = line.split("\t");

			while ((line = br.readLine()) != null) {

				split = line.split("\t");
				String classOfRow = split[split.length - 1];
				
				if (classCount.get(classOfRow) == null) {
					classCount.put(classOfRow, new ArrayList<Integer>());
					classCount.get(classOfRow).add(1);
				} else if (classCount.get(classOfRow) != null) {
					classCount.get(classOfRow).add(1);
				}

				for (int i = 0; i < split.length - 1; i++) {

					if (data.get(classOfRow) == null) {

						data.put(classOfRow, new HashMap<Integer, ArrayList<String>>());
						classKeys.add(classOfRow);
					}

					if (data.get(classOfRow).get(i + 1) == null) {

						data.get(classOfRow).put(i + 1, new ArrayList<String>());
					}

					data.get(classOfRow).get(i + 1).add(split[i]);

				}

			}
			br.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void test(String filename) {

		try {
			int count = 0;
			int total = 0;
			BufferedReader br = new BufferedReader(new FileReader(filename));
			
			
			String line = br.readLine();
			System.out.println("F1\tF2\tF3\tF4\tF5\tCLASS\tPREDICT\tRESULT");
			System.out.println("--\t--\t--\t--\t--\t-----\t-------\t------");

			while ((line = br.readLine()) != null) {

				total++;
				String[] split = line.split("\t");
				String classified = classify(split[0], split[1], split[2], 
						Float.parseFloat(split[3]),Float.parseFloat(split[4]));
				String correct = "";
				if (classified.equals(split[split.length - 1])) {
					correct = "CORRECT";
					count++;
				} else {
					correct = "INCORRECT";
				}
				System.out.println(split[0] + "\t" + split[1] + "\t" + split[2] + "\t" + split[3] + 
									"\t" + split[4]+ "\t" + split[split.length - 1]
											+ "\t" + classified + "\t" + correct);

			}
			double accuracy = (double) (count * 100) / (total);
			System.out.println("\n");
			System.out.println(
					"Total Accuracy:\t" + count + " correct / " + total + " total = " + 
							accuracy + "% Accuracy");
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String classify(String f1, String f2, String f3, float f4, float f5) {

		int classTotal = 0;
		for (ArrayList<Integer> j : classCount.values()) {

			classTotal += j.size();
		}

		String[] split = { f1, f2, f3, f4 + "", f5 + "" };
		HashMap<String, Double> probabilities = new HashMap<>();
		for (String y : classKeys) {

			double top = (double) classCount.get(y).size() / classTotal;
			double bottom = 0;

			for (String j : classKeys) {
				double classes = (double) classCount.get(j).size() / classTotal;
				for (int i = 1; i < 5; i++) {

					if (i < 4) {

						classes *= DiscreteProbFeatures(data.get(j).get(i), split[i - 1]);
					} else {
						classes *= cdf(Float.parseFloat(split[3]), mean(data.get(j).get(i)), 
								stdv(data.get(j).get(i)));
						classes *= cdf(Float.parseFloat(split[4]), mean(data.get(j).get(i + 1)),
								stdv(data.get(j).get(i + 1)));
					}
				}
				bottom += classes;
			}

			for (int i = 1; i < 5; i++) {

				if (i < 4) {
					top *= DiscreteProbFeatures(data.get(y).get(i), split[i - 1]);
				} else {
					top *= cdf(Float.parseFloat(split[3]), 
							mean(data.get(y).get(i)), stdv(data.get(y).get(i)));
					top *= cdf(Float.parseFloat(split[4]), 
							mean(data.get(y).get(i + 1)), stdv(data.get(y).get(i + 1)));
				}
			}

			probabilities.put(y, top / bottom);

		}
		double max = 0;
		String classifiedClass = "";
		for (int i = 0; i < probabilities.size(); i++) {
			if (probabilities.get(classKeys.get(i)) > max) {
				max = probabilities.get(classKeys.get(i));
				classifiedClass = classKeys.get(i);
			}

		}
		return classifiedClass;
	}

	public double DiscreteProbFeatures(ArrayList<String> features, String x) {

		double count = 0;

		for (int i = 0; i < features.size(); i++) {
			if (features.get(i).equals(x)) {
				count++;
			}
		}

		return count / features.size();
	}

	public double mean(ArrayList<String> list) {
		double sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += Float.parseFloat(list.get(i));
		}
		return sum / list.size();
	}

	public double stdv(ArrayList<String> list) {
		double mean = mean(list);
		double sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += Math.pow(Float.parseFloat(list.get(i)) - mean, 2);
		}
		return Math.sqrt(sum / list.size());
	}


	public static double pdf(double x) {
		return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
	}

	public static double pdf(double x, double mu, double sigma) {
		return pdf((x - mu) / sigma) / sigma;
	}

	public static double cdf(double z) {
		if (z < -8.0)
			return 0.0;
		if (z > 8.0)
			return 1.0;
		double sum = 0.0, term = z;
		for (int i = 3; sum + term != sum; i += 2) {
			sum = sum + term;
			term = term * z * z / i;
		}
		return 0.5 + sum * pdf(z);
	}

	public static double cdf(double z, double mu, double sigma) {
		return cdf((z - mu) / sigma);
	}
}
