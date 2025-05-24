package app.preach.gospel.config;

import org.apache.struts2.dispatcher.filter.StrutsPrepareAndExecuteFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import jakarta.servlet.Filter;
import lombok.extern.log4j.Log4j2;

/**
 * Struts2設定クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Log4j2
@Configuration
public class ServletFilterConfiguration {

	/**
	 * Strutsの基本フィルタを配置する
	 *
	 * @return StrutsPrepareAndExecuteFilter
	 */
	@Order(10)
	@Bean
	protected FilterRegistrationBean<Filter> struts2Filter() {
		final FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
		filter.setFilter(new StrutsPrepareAndExecuteFilter());
		filter.setName("struts2");
		filter.addUrlPatterns("/*");
		log.info("Struts2フレームワーク配置成功！");
		return filter;
	}

}
