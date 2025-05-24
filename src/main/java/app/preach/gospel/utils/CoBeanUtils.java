package app.preach.gospel.utils;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 共通Beanツール
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoBeanUtils extends BeanUtils {

    /**
     * NULLではないプロパティだけコピーする
     *
     * @param source コピー元
     * @param target コピー先
     * @throws BeansException 例外
     */
    public static void copyNullableProperties(final Object source, final Object target) throws BeansException {
        BeanUtils.copyProperties(source, target, CoBeanUtils.getNullProperties(source));
    }

    /**
     * コピー元のNULLプロパティを取得する
     *
     * @param source コピー元
     * @return NULLプロパティ
     */
    private static String @NotNull [] getNullProperties(final Object source) {
        final BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        final PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
        final Set<String> nullFields = new HashSet<>();
        for (final PropertyDescriptor p : propertyDescriptors) {
            final String name = p.getName();
            final Object value = beanWrapper.getPropertyValue(name);
            if (value == null) {
                nullFields.add(name);
            }
        }
        return nullFields.toArray(String[]::new);
    }

}
