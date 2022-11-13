package cl;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

public class ClusterStars {

	public static void main(String[] args) {
		Model model = ModelFactory.createDefaultModel() ;
		model.read("C:\\Users\\scfer\\Documents\\Universidad\\Publicaciones\\Extended ISWC 2020\\stars3.ttl") ;
		
		String s = "PREFIX sim:<http://sj.dcc.uchile.cl/sim#>\n"
				+ "PREFIX fno:<http://w3id.org/function/ontology#>\n"
				+ "PREFIX star:<http://stars.dcc.uchile.cl/star/>\n"
				+ "PREFIX stp:<http://stars.dcc.uchile.cl/prop/>\n"
				+ "SELECT ?temp ?lum ?c WHERE {\n"
				+ "?star stp:color ?temp ; stp:absMagnitude ?lum ; stp:parallaxUncertainty ?error .\n"
				+ "FILTER (?error < 75) \n"
				+ "BIND((?lum+1.444394)/(14.712036999999999 + 1.444394) AS ?lum2) \n"
				+ "BIND((?temp+0.17)/(1.88+0.17) AS ?temp2)"
				//+ "BIND(1 AS ?c)"
				+ "}\n"
				//;
				+ "CLUSTER BY ?temp2 ?lum2 AS ?c\n"
				+ "WITH ("
				+ "_:x fno:executes sim:kmedoids ; sim:numberOfClusters 3"
				+ ")";
		
		Query query = QueryFactory.create(s, Syntax.syntaxSPARQL_11_sim) ;
		
        Op op = Algebra.compile(query) ;

        QueryIterator qIter = Algebra.exec(op, model) ;
        while(true){
            Binding b = qIter.nextBinding() ;
            System.out.printf("%f;%f;%d\n", ((Number)b.get("temp").getLiteralValue()).doubleValue(), 
            		((Number)b.get("lum").getLiteralValue()).doubleValue(), 
            		((Number)b.get("c").getLiteralValue()).intValue()) ;
           
        }
	}
	
}
