/**
 * 
 */
package demo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author gazal
 */
@Component
public class SpringApplicationContext implements ApplicationContextAware {

	private static ApplicationContext CONTEXT;

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(final ApplicationContext context) throws BeansException {
		CONTEXT = context;
	}

	public static <T> T getBean(Class<T> clazz) {
		return CONTEXT.getBean(clazz);
	}

}
