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
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.tdb.TDBFactory;

public class RunCounts {
	
	public static void main(String[] args) throws IOException {
		Dataset dataset = TDBFactory.createDataset("F:\\wikidata");
		Model m = dataset.getDefaultModel();
		
		String queryPath = "C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\fullquerylog\\counts\\";
		
		BufferedWriter writer = new BufferedWriter( 
				new FileWriter("C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\rescount.txt", true));
		List<String> doneFiles = new LinkedList<String>();
		List<String> countFiles = new LinkedList<String>();
		
		try (
				Stream<String> streamDone = Files.lines(Paths.get("C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\done.txt"), StandardCharsets.UTF_8);
				Stream<String> streamFlann = Files.lines(Paths.get("C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\doneCount.txt"), StandardCharsets.UTF_8);)
        {
            streamDone.forEach(s -> doneFiles.add(s));
            streamFlann.forEach(s -> countFiles.add(s));
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
		
		BufferedWriter doneWriter = new BufferedWriter(
		        new FileWriter("C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\done.txt", true));
		BufferedWriter flannWriter = new BufferedWriter(
		        new FileWriter("C:\\Users\\scfer\\Documents\\Universidad\\Doctorado\\Jena\\doneCount.txt", true));
		for (String filename : doneFiles) {
			System.out.println(filename);
			doneWriter.flush(); writer.flush(); flannWriter.flush();
			if(countFiles.contains(filename)) {
				countFiles.remove(filename);
				continue;
			}
			String simQuery = readFile(queryPath + filename);
			long start = System.nanoTime();
		    Query simquery = QueryFactory.create(String.format(simQuery), Syntax.syntaxSPARQL_11_sim) ;
		    Op op = Algebra.compile(simquery) ;
		    op = Algebra.optimize(op) ;
		    QueryIterator qIter = Algebra.exec(op, m) ;
		    int count = 0;
		    for ( ; true ; ){
		     	try {
		       		Binding res = qIter.nextBinding() ;
		       		count = Integer.parseInt(res.get("c").getLiteralValue().toString());
		       	} catch (NoSuchElementException e) {
		       		break;
		      	}
		    }
		    long end = System.nanoTime();
		    double time = ((end-start)/1000000000.0);
			writer.write(filename + ";" + time + ";" + count + "\n");
			flannWriter.write(filename + "\n");
		}
		writer.close();
		flannWriter.close();
		doneWriter.close();
	}
	
	private static String readFile(String absolutePath) {
		StringBuilder contentBuilder = new StringBuilder();
		 
        try (Stream<String> stream = Files.lines( Paths.get(absolutePath), StandardCharsets.UTF_8)) 
        {
            stream.forEach(s -> {if(!s.trim().equals("")) contentBuilder.append(s).append("\n");});
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
 
        return contentBuilder.toString();
	}
}
