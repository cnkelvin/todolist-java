package br.com.kelvinnielson.todolist.utils;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;


// Esse código vai servir ao ResponseEntity update(),
// aonde ele vai substituir as informações nulas da nova alteração
// para as informações anteriores que já tinham na tarefa.

/* Exemplo: o usuário sem esse código alterava apenas a descrição da tarefa,
	 então o resto das outras informações retornavam null. O código pega essas
	 informações nulas e substitui para as informações que já continham na tarefa
	 anteriormente, alterando apenas o que o usuário requisitou.
*/
public class Utils {

	public static void copyNonNullProperties(Object source, Object target) {
		BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
	}

	public static String[] getNullPropertyNames(Object source) {
		final BeanWrapper src = new BeanWrapperImpl(source);

		PropertyDescriptor[] pds = src.getPropertyDescriptors();

		Set<String> emptyNames = new HashSet<>();

		for(PropertyDescriptor pd: pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if(srcValue == null) {
				emptyNames.add(pd.getName());
			}
		}

		String[] result = new String[emptyNames.size()];
		return emptyNames.toArray(result);
	}
}