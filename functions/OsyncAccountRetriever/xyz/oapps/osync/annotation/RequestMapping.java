package xyz.oapps.osync.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)  

public @interface RequestMapping {
	String method() default "";

	String path();

	String produces() default "application/json";
	
	int accessLevel() default 4;
}
