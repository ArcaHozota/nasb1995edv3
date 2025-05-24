package app.preach.gospel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.common.ProjectURLConstants;
import lombok.extern.log4j.Log4j2;

/**
 * SpringMVC配置クラス
 *
 * @author ArkamaHozota
 * @since 6.11
 */
@Log4j2
@Configuration
public class SpringWebMvcConfiguration implements WebMvcConfigurer {

    /**
     * 静的なリソースのマッピングを設定する
     *
     * @param registry レジストリ
     */
    @Override
    public void addResourceHandlers(final @NonNull ResourceHandlerRegistry registry) {
        log.info(ProjectConstants.MESSAGE_SPRING_MAPPER);
        registry.addResourceHandler(ProjectURLConstants.URL_STATIC_RESOURCE).addResourceLocations("classpath:/static/");
    }

}
