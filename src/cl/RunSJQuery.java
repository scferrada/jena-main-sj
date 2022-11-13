package cl;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

public class RunSJQuery {
	public static void main(String[] args) {
		String s = "PREFIX ex:<http://ex.com/> "
				+ "SELECT DISTINCT ?s ?s2 ?x WHERE { { ?s ex:a ?a; ex:b ?b } "
				+ "SIMILARITY JOIN on (?a, ?b) (?a2,?b2) "
				+ "TOP 2 DISTANCE ex:manhattan as ?x"
				+ " { ?s2 ex:a ?a2; ex:b ?b2 }} "
				+ "ORDER BY ?s";
		//String s = "SELECT DISTINCT ?s WHERE {?s ?p ?o}";
		
		/*Dataset dataset = TDBFactory.createDataset("F:\\wikidata");
		Model m = dataset.getDefaultModel();
		
		String s1 = "PREFIX wdt:<http://www.wikidata.org/prop/direct/> "
				+ "PREFIX wd:<http://www.wikidata.org/entity/> "
				+ "PREFIX ex:<http://ex.com/>"
				+ "SELECT DISTINCT ?city1 ?city2 ?d WHERE {"
				+ " { SELECT ?city1 (COUNT (?canal1) AS ?c1) WHERE{"
				+ "		?canal1 wdt:P31 ?iof1 ."
				+ "		?iof1 wdt:P279* wd:Q12284 ."
				+ "		?canal1 wdt:P131 wd:Q9899 ."
				+ "BIND(wd:Q9899 AS ?city1)"
				+ "} GROUP BY ?city1 } "
				+ "SIMILARITY JOIN on (?c1) (?c2) "
				+ "TOP 10 DISTANCE ex:manhattan as ?d"
				+ " { SELECT ?city2 (COUNT (?canal2) AS ?c2) WHERE{ "
				+ "			?canal2 wdt:P31 ?iof2 ."
				+ "			?iof1 wdt:P279* wd:Q12284 ."
				+ "			?canal1 wdt:P131 ?city1 ."
				+ "	} GROUP BY ?city2 }} "
				+ "ORDER BY ?s";
		
		String s2 ="PREFIX wdt:<http://www.wikidata.org/prop/direct/> "
				+ "PREFIX wd:<http://www.wikidata.org/entity/> "
				+ "PREFIX ex:<http://ex.com/> " 
				+ "SELECT ?city1 (COUNT (?canal1) AS ?c1) WHERE{"
				+ "		?canal1 wdt:P31/wdt:P279* wd:Q12284 ."
				+ "		?canal1 wdt:P131 ?city1 ."
				+ "} GROUP BY ?city1";*/
        
        // Parse
        Query query = QueryFactory.create(s, Syntax.syntaxSPARQL_11_sim) ;
        
        
        // Generate algebra
        Op op = Algebra.compile(query) ;
        op = Algebra.optimize(op) ;
        
        System.out.println(op);
        
        // Execute it.
        Model m = createSimModel();
        QueryIterator qIter = Algebra.exec(op, m) ;
        
        while(true) {
        	Binding b = qIter.nextBinding() ;
            System.out.println(b) ;
        }
        
        // Results
        /*for ( ; qIter.hasNext() ; )
        {
            Binding b = qIter.nextBinding() ;
            System.out.println(b) ;
        }*/
	}
	
	public static Model createSimModel(){
        Model m = ModelFactory.createDefaultModel();
        Property a = m.createProperty("http://ex.com/a");
        Property b = m.createProperty("http://ex.com/b");
        int N = 10;
        for (int i = 0; i < N; i++) {
            Resource r = m.createResource("http://ex.com/"+i);
            r.addProperty(a, ""+i, XSDDatatype.XSDdouble).addProperty(b, ""+i, XSDDatatype.XSDdouble);
        }
        return m;
    }
}
