package cl;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Dataset;
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
import org.apache.jena.tdb.TDBFactory;

public class RunClusterQuery {

	public static void main(String[] args) {
		//Dataset dataset = TDBFactory.createDataset("F:\\wikidata");
		Model m = createSimModel();//dataset.getDefaultModel();
		String s = "PREFIX wdt:<http://www.wikidata.org/prop/direct/>\n" + 
				"PREFIX wd:<http://www.wikidata.org/entity/>\n" +
				"PREFIX ex:<http://ex.com/>\n" +
				"PREFIX sim:<http://sj.dcc.uchile.cl/sim#>\n" +
				"SELECT ?a ?b ?c WHERE {"
				+ "?c1 ex:a ?a ; "
				+ "ex:b ?b .} "
				+ "CLUSTER BY ?a ?b "
				+ "WITH sim:kmeans(3, 11, 3)	 AS ?c";
		Query query = QueryFactory.create(s, Syntax.syntaxSPARQL_11_sim) ;
		
        Op op = Algebra.compile(query) ;
        
        //System.out.println(op);

        QueryIterator qIter = Algebra.exec(op, m) ;
        while(true){
            Binding b = qIter.nextBinding() ;
            System.out.println(b) ;
        }
	}
	
	public static Model createSimModel(){
        Model m = ModelFactory.createDefaultModel();
        Property a = m.createProperty("http://ex.com/a");
        Property b = m.createProperty("http://ex.com/b");
        int N = 100;
        for (int i = 0; i < N; i++) {
            Resource r = m.createResource("http://ex.com/"+i);
            r.addProperty(a, ""+i, XSDDatatype.XSDdouble).addProperty(b, ""+i, XSDDatatype.XSDdouble);
        }
        return m;
    }
	
}
