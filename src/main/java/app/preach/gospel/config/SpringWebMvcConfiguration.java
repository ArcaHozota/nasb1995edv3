package app.preach.gospel.config;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.common.ProjectURLConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * SpringMVC配置クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Slf4j
@Configuration
public class SpringWebMvcConfiguration extends WebMvcConfigurationSupport {

	/**
	 * 静的なリソースのマッピングを設定する
	 *
	 * @param registry レジストリ
	 */
	@Override
	protected void addResourceHandlers(final @NotNull ResourceHandlerRegistry registry) {
		log.info(ProjectConstants.MESSAGE_SPRING_MAPPER);
		registry.addResourceHandler(ProjectURLConstants.URL_STATIC_RESOURCE).addResourceLocations("classpath:/static/");
	}

	/**
	 * ビューのコントローラを定義する
	 *
	 * @param registry
	 */
	@Override
	public void addViewControllers(final @NotNull ViewControllerRegistry registry) {
		registry.addViewController(ProjectURLConstants.URL_CATEGORY_NAMESPACE.concat(ProjectURLConstants.URL_TO_LOGIN))
				.setViewName("logintoroku");
		registry.addViewController(
				ProjectURLConstants.URL_CATEGORY_NAMESPACE.concat(ProjectURLConstants.URL_TO_MAINMENU))
				.setViewName("mainmenu");
		registry.addViewController(
				ProjectURLConstants.URL_HYMNS_NAMESPACE.concat(ProjectURLConstants.URL_TO_RANDOM_FIVE))
				.setViewName("hymns-random-five");
	}

	/**
	 * SpringMVCフレームワークを拡張するメッセージ・コンバーター
	 *
	 * @param converters コンバーター
	 */
	@Override
	protected void extendMessageConverters(final @NotNull List<HttpMessageConverter<?>> converters) {
		log.info(ProjectConstants.MESSAGE_SPRING_MVCCONVERTOR);
		// メッセージコンバータオブジェクトを作成する。
		final MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
		// オブジェクトコンバータを設定し、Jacksonを使用してJavaオブジェクトをJSONに変換する。
		messageConverter.setObjectMapper(new JacksonObjectMapper());
		// 上記のメッセージコンバータをSpringMVCフレームワークのコンバータコンテナに追加する。
		converters.add(0, messageConverter);
	}

}
