package ca.pandp.shared.domainobjects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 07, 2015.
 * <p>
 * This interface represents the model object that is used to transfer the data model from producers to consumers.
 */

public interface BeanModel {

    /**
     * @return The list of annotations on this method, for example, ca.pandp.shared.annotations.Generates
     */
    List<Annotation> getAnnotations();

    /**
     * @return The name of the bean being generated, i.e. Person (for a bean that represents a person)
     */
    String getClassName();

    /**
     * @return The name of the destination package, i.e. Person might be in the package, com.awesome
     */
    String getPackageName();

    /**
     * @return A list of method signatures belonging to the bean being produced, i.e. Person might have public Name getName();
     */
    List<MethodModel> getMethods();


    interface MethodModel {
        /**
         * @return The list of annotations on this method, for example, javax.annotation.NotNull
         */
        List<Annotation> getAnnotations();

        /**
         * @return The list of method modifiers, for example, abstract.
         */
        List<String> getMethodModifiers();

        /**
         * @return The return type of the method, or example, on Person there could be a method Name getName(); where the return type is Name
         */
        String getMethodReturnType();

        /**
         * @return The name of the method, for example, on Person, there could be methods getName(); or isFemale(); and this would return name and female, respectively
         */
        String getMethodName();
    }
}
