package cl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.tdb.TDBFactory;

public class SJExperiments {
	
	public static void main(String[] args) throws IOException {
		Dataset dataset = TDBFactory.createDataset("F:\\wikidata");
		Model m = dataset.getDefaultModel();
		
		String queryPath = "C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\simjoins\\";
		
		int repetitions = 1;
		
		BufferedWriter writer = new BufferedWriter( 
				new FileWriter("C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\resflann2.txt", true));
		
		List<String> doneFiles = new LinkedList<String>();
		List<String> flannFiles2 = new LinkedList<String>();
		List<String> flannFiles4 = new LinkedList<String>();
		List<String> flannFiles8 = new LinkedList<String>();
		
		try (
				Stream<String> streamDone = Files.lines(Paths.get("C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\done.txt"), StandardCharsets.UTF_8);
				Stream<String> streamFlann = Files.lines(Paths.get("C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\doneflann2.txt"), StandardCharsets.UTF_8);)
        {
            streamDone.forEach(s -> doneFiles.add(s));
            streamFlann.forEach(s -> {
            		String[] parts = s.split(";");
            		if (parts[1].equals("2"))
            			flannFiles2.add(parts[0]);
            		else if (parts[1].equals("4"))
            			flannFiles4.add(parts[0]);
            		else if (parts[1].equals("8"))
            			flannFiles8.add(parts[0]);
            	});
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
		
		BufferedWriter doneWriter = new BufferedWriter(
		        new FileWriter("C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\done.txt", true));
		BufferedWriter flannWriter = new BufferedWriter(
		        new FileWriter("C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\doneflann2.txt", true));
		for (int k : new int[]{2,4,8}) {
			boolean cancel = false;
			for (String filename : doneFiles) {
				System.out.println(filename);
				doneWriter.flush(); writer.flush(); flannWriter.flush();
				if(k==2 && flannFiles2.contains(filename)) {
					flannFiles2.remove(filename);
					continue;
				}
				else if(k==4 && flannFiles4.contains(filename)) {
					flannFiles4.remove(filename);
					continue;
				}
				else if(k==8 && flannFiles8.contains(filename)) {
					flannFiles8.remove(filename);
					continue;
				}
				String simQuery = readFile(queryPath + filename);
				List<Double> times = new LinkedList<Double>();
				System.out.println(k);
				for(int i=0; i < repetitions; i++) {
					long start = System.nanoTime();
			        Query simquery = QueryFactory.create(String.format(simQuery, k), Syntax.syntaxSPARQL_11_sim) ;
			        Op op = Algebra.compile(simquery) ;
			        op = Algebra.optimize(op) ;
			        QueryIterator qIter = Algebra.exec(op, m) ;
			        for ( ; true ; ){
			        	try {
			        		qIter.nextBinding() ;
			        	} catch (NoSuchElementException e) {
			        		break;
			        	}
			        }
			        long end = System.nanoTime();
			        times.add((end-start)/1000000000.0);
				}
				double sum = times.stream().reduce(0.0, (x, y) -> x+y);
				double avg = sum/times.size();
				double variance = times.stream().map(x->(x-avg)*(x-avg)).reduce(0.0, (x,y)-> x+y);
				writer.write(filename + ";" + k + ";" + avg + ";" + variance + "\n");
				flannWriter.write(filename +";"+ k + "\n");
			}	
			if(cancel) continue;
		}
		writer.close();
		flannWriter.close();
		doneWriter.close();
	}
	
	private static String readFile(String absolutePath) {
		StringBuilder contentBuilder = new StringBuilder();
		 
        try (Stream<String> stream = Files.lines( Paths.get(absolutePath), StandardCharsets.UTF_8)) 
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
 
        return contentBuilder.toString();
	}

}
