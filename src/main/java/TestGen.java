import java.io.File;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

public class TestGen {
	
	public static void main(String[] args) throws Exception {		
		
		File srcFile = new File("src/main/java/com/bookstore/controller/HomeController.java");
		CompilationUnit cu;
        cu = JavaParser.parse(srcFile);
        
        List<TypeDeclaration<?>> types = cu.getTypes();
        for (TypeDeclaration type : types) {
        List<BodyDeclaration> members = type.getMembers();
	        for (BodyDeclaration member : members) {
	            if (member instanceof MethodDeclaration) {
	            	MethodDeclaration methodDec = (MethodDeclaration) member;
	            	if (methodDec.getAnnotationByName("RequestMapping").isPresent()) {
//	            		NodeList<AnnotationExpr> list = methodDec.getAnnotations();
//	            		for (AnnotationExpr ae : list) {
//	            			System.out.println(ae.getNameAsString());
//	            		}
	            		NodeList<Parameter> paramLlist = methodDec.getParameters();
	            		for (Parameter p : paramLlist) {
	            			NodeList<AnnotationExpr> paramAnnotationList = p.getAnnotations();
	            			for (AnnotationExpr ae : paramAnnotationList) {
	            				System.out.println(ae.getNameAsString());
	            			}
	            			//System.out.println(p.getNameAsString());
	            		}
	            	}
	            }
	        }
        }		
	}
}
