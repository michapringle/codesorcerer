package ca.pandp.builder;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public final class BeautifulBeanValidator
{
	public static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	public static final Validator validator = factory.getValidator();
}
