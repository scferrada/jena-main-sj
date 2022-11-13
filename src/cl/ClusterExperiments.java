package cl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.tdb.TDBFactory;

public class ClusterExperiments {
	public static void main(String[] args) throws IOException {
		Dataset dataset = TDBFactory.createDataset("F:\\wikidata");
		Model model = dataset.getDefaultModel();
		
		String queryPath = "C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\cluster\\";
		File queryDir = new File(queryPath);
		
		int repetitions = 5;
		
		String[] algorithms = new String[] {"sim:kmeans", "sim:kmedoids", "sim:dbscan"};
		int[] ks = new int[] {4, 8, 12, 16};
		int[] ms = new int[] {10, 20, 30};
		double[] epsilons = new double[] {0.01, 0.1, 1};
		int[] minPs = new int[] {1, 10, 100};
		
		BufferedWriter kmeansWriter = new BufferedWriter(new FileWriter("/path/to/file", true));
		BufferedWriter kmedoidsWriter = new BufferedWriter(new FileWriter("/path/to/file", true));
		BufferedWriter dbscanWriter = new BufferedWriter(new FileWriter("/path/to/file", true));
		long start, end;
		
		for (int i = 0; i < repetitions; i++) {
			for (File file : queryDir.listFiles()) {
				String template = Files.readString(file.toPath());
				for(String algorithm : algorithms) {
					if (algorithm.equals("sim:kmeans")) {
						for (int k : ks) {
							for (int m : ms) {
								String query = String.format(template, algorithm + "( "+k +" " + m +" )");
								start = System.nanoTime();
								runquery(query, model);
								end = System.nanoTime();
								kmeansWriter.append(String.format("%s;%d;%d;%d;%d\n", file.getName(), i, k, m, end-start));
							}
						}
					} else if (algorithm.equals("sim:kmedoids")) {
						for (int k : ks) {
							String query = String.format(template, algorithm + "( "+ k + " )");
							start = System.nanoTime();
							runquery(query, model);
							end = System.nanoTime();
							kmedoidsWriter.append(String.format("%s;%d;%d;%d\n", file.getName(), i, k, end-start));
						}
					} else {
						for (double epsilon : epsilons) {
							for (double minP : minPs) {
								String query = String.format(template, algorithm + "( " + epsilon +" "+ minP +" )");
								start = System.nanoTime();
								runquery(query, model);
								end = System.nanoTime();
								dbscanWriter.append(String.format("%s;%d;%f;%d;%d\n", file.getName(), i, epsilon, minP, end-start));
							}
						}
					}
				}
			}
		}
	}

	private static void runquery(String query, Model model) {
		Query q = QueryFactory.create(query, Syntax.syntaxSPARQL_11_sim) ;
		
        Op op = Algebra.compile(q) ;

        QueryIterator qIter = Algebra.exec(op, model) ;
        while(true){
            Binding b = qIter.nextBinding() ;
        }
	}
}
