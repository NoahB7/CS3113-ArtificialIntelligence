import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class UASimpleClassifierClone {

	/********************************
	 * Name: Noah Buchanan Username: ua100 Problem Set: PS3 Due Date: Feb 11, 2021
	 ********************************/

	HashMap<String, ArrayList<ArrayList<String>>> data = new HashMap<>();
	HashMap<String, HashMap<String,Double>> prob = new HashMap<>();
	HashMap<String, ArrayList<Integer>> classCount = new HashMap<>();
	ArrayList<String> keys = new ArrayList<>();

	public static void main(String[] args) {

		UASimpleClassifierClone a = new UASimpleClassifierClone();
		a.train("train.txt");
		a.test("test.txt");
	}

	public void train(String filename) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(filename));

			String line = br.readLine();

			while ((line = br.readLine()) != null) {
				String[] split = line.split(",");
				String c = split[split.length - 1];
				if (data.get(c) == null) {
					classCount.put(c, new ArrayList<Integer>());
					keys.add(c);
					data.put(c, new ArrayList<ArrayList<String>>());
					prob.put(c, new HashMap<String,Double>());
					for (int i = 0; i < 5; i++) {
						data.get(c).add(new ArrayList<String>());
					}
				}
				classCount.get(c).add(1);
				for (int i = 0; i < 5; i++) {
					data.get(c).get(i).add(split[i]);
					if(prob.get(c).get(split[i]) == null) {

					}
				}
			}

			ArrayList<String> bag;

			for (String key : keys) {
				bag = new ArrayList<>();
				for (int j = 0; j < data.get(key).size(); j++) {
					for (int k = 0; k < data.get(key).get(j).size(); k++) {
						if (j < 3) {
							
							if(prob.get(key).get(data.get(key).get(j).get(k)) == null) {
								prob.get(key).put(data.get(key).get(j).get(k), discreteProb(data.get(key).get(j), data.get(key).get(j).get(k)));
							}
						} else {

							if(prob.get(key).get(data.get(key).get(j).get(k)) == null) {
								prob.get(key).put(data.get(key).get(j).get(k), cdf(Double.parseDouble(data.get(key).get(j).get(k)), mean(data.get(key).get(j)), stdv(data.get(key).get(j))));
							}
						}
						System.out.println(data.get(key).get(j).get(k) + " prob : " + prob.get(key).get(j));

					}
				}
			}
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}

	public void test(String filename) {

		try {
			
			int total = 0;
			int count = 0;
			BufferedReader br = new BufferedReader(new FileReader(filename));

			String line = br.readLine();
			
			//System.out.println("F1\tF2\tF3\tF4\tF5\tCLASS\tPREDICT\tRESULT");
			//System.out.println("--\t--\t--\t--\t--\t-----\t-------\t------");


			while ((line = br.readLine()) != null) {

				total++;
				String[] split = line.split(",");

				String c = classify(split[0],split[1],split[2],Integer.parseInt(split[3]),Integer.parseInt(split[4]));
				String correct = "";
				if(c.equals(split[split.length-1])) {
					correct = "CORRECT";
					count++;
				} else {
					correct = "INCORRECT";
				}
				//System.out.println(split[0]+"\t"+split[1]+"\t"+split[2]+"\t"+split[3]+"\t"+split[4]+"\t"+split[split.length-1]+"\t"+c+"\t"+correct);
				
			}
			double accuracy = (double)(count*100)/(total);
			//System.out.println("\n");
			//System.out.println("Total Accuracy:\t" + count + " correct / " + total + " total = "+ accuracy + "% Accuracy");
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}

	public String classify(String f1, String f2, String f3, double f4, double f5) {
		double total = 0;
		for(String x: keys) {
			total += classCount.get(x).size();
		}
		
		String[] split = { f1, f2, f3, f4 + "", f5 + "" };
		HashMap<String,Double> probabilities = new HashMap<>();
		
		for (String key : keys) {
			
			double top = classCount.get(key).size()/total;
			System.out.println("class_j prob : " + top);
			double bottom = 0;
			for (int j = 0; j < prob.get(key).size(); j++) {
					
				top *= prob.get(key).get(split[j]);
				
			}

			for (String c : keys) {
				double hold = classCount.get(c).size()/total;
				System.out.println("class_n prob : " + hold);
				for (int j = 0; j < prob.get(c).size(); j++) {
					
					hold *= prob.get(c).get(split[j]);
				}
				bottom += hold;
			}
			
			probabilities.put(key,top/bottom);
		}
		
		double max = 0;
		String classified = "";
		for(String y : keys) {
			if(probabilities.get(y) > max) {
				max = probabilities.get(y);
				classified = y;
			}
		}
		
		return classified;
	}

	public double discreteProb(ArrayList<String> list, String var) {

		double prob = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(var)) {
				prob++;
			}
		}

		return prob / list.size();
	}

	public double mean(ArrayList<String> list) {
		double sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += Integer.parseInt(list.get(i));
		}
		return sum / list.size();
	}

	public double stdv(ArrayList<String> list) {
		double mean = mean(list);
		double sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += Math.pow(Integer.parseInt(list.get(i)) - mean, 2);
		}
		return Math.sqrt(sum / list.size() - 1);
	}

	// non categorical data probability
	// taken from https://introcs.cs.princeton.edu/java/22library/Gaussian.java.html

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