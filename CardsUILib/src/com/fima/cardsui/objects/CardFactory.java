package com.fima.cardsui.objects;

import java.lang.reflect.Field;
import java.util.HashMap;

import android.util.Log;

/**
 * Contains method(s) to create a {@link Card}-family object from its
 * serializable model.
 * 
 * <p>
 * Note that any similarities to Card Factory Ltd
 * <http://www.cardfactory.eu.com/> are purely accidental.
 * 
 * @author FLamparski
 * 
 */
public class CardFactory {
	/**
	 * Uses Reflection to create a new {@link AbstractCard} from the given
	 * {@link CardModel} by copying fields.
	 * 
	 * @param model
	 *            The {@link CardModel} to "inflate" into an
	 *            {@link AbstractCard}
	 * @return An {@link AbstractCard} that matches the model data
	 * @throws InstantiationException
	 *             Thrown when the class specified by the model cannot be
	 *             instantiated (no default ctor).
	 * @throws IllegalAccessException
	 *             Thrown if I missed a setAccessible(true) somewhere, or if the
	 *             default ctor for the target class is not visible.
	 */
	public static AbstractCard createCard(CardModel model)
			throws InstantiationException, IllegalAccessException {
		/*
		 * Instantiate a new object (must be AbstractCard or extend it, makes
		 * sense for the CardsUI mechanic) from a Class descriptor.
		 */
		AbstractCard newCard = model.cardClass.newInstance();
		Log.i("CardFactory", "Creating a new card! We're making a new "
				+ model.cardClass.getName());

		/*
		 * We will also need the Class descriptor for the object we just created
		 * in order to access its fields.
		 */
		Class<? extends AbstractCard> newCardClazz = newCard.getClass();

		/*
		 * Java objects do not flatten their hierarchy at runtime (that's good),
		 * which means that we actually need to collect all fields from all the
		 * ancestors. Here, we collect them into a HashMap for convenience.
		 */
		HashMap<String, Field> destinationFields = new HashMap<String, Field>();
		Class<?> clazzUnderInspection = newCardClazz;
		while (clazzUnderInspection != null) {
			/*
			 * Get all fields for the current point in the hierarchy, and
			 * collect them into the HashMap.
			 */
			Field[] clazzFields = clazzUnderInspection.getDeclaredFields();
			for (int i = 0; i < clazzFields.length; i++) {
				Field f = clazzFields[i];
				destinationFields.put(f.getName(), f);
			}
			/*
			 * Okay, now examine the ancestor (for java.lang.Object it is null,
			 * which means the loop will exit).
			 */
			clazzUnderInspection = clazzUnderInspection.getSuperclass();
		}

		/*
		 * Obtain a list of fields within the model. As most of them match those
		 * in AbstractCard, the card's content will be preserved. Since the
		 * model inherits only from Object, all fields collected here will be
		 * what we need.
		 */
		Field[] sourceFields = model.getClass().getDeclaredFields();

		/*
		 * Now iterate over the fields in the model.
		 */
		for (int i = 0; i < sourceFields.length; i++) {
			// Just a reference for the field we're copying in this pass
			Field curField = sourceFields[i];

			/*
			 * This is to prevent IllegalAccessExceptions when accessing the
			 * field. Yes, this violates the visibility set by the field
			 * declarations, but that's the whole point of this routine.
			 */
			curField.setAccessible(true);

			String fieldName = curField.getName();

			Log.d("CardFactory", " > Now copying field: " + fieldName);

			Field destField = destinationFields.get(fieldName);

			if (destField != null) {
				/*
				 * We need to get the specific field that matches the one in the
				 * model and set it to the same value as the corresponding field
				 * in the model.
				 */
				destField.setAccessible(true); // "Trust me."
				destField.set(newCard, curField.get(model));
				Log.d("CardFactory", String.format(
						" > Field %s (= %s) -> Field %s (=%s)", curField,
						curField.get(model), destField.getName(),
						destField.get(newCard)));
			} else {
				/*
				 * We have encountered a field (CardModel.data,
				 * CardModel.cardClass, ...) in the model that does not exist in
				 * the view that can represent it, so we just skip it. Yes, this
				 * breaks the "for loop is fixed" convention, but this really is
				 * the easiest way.
				 * 
				 * "Go to Next Iteration. Go directly to Next Iteration. Do not
				 * pass Go. Do not collect $200."
				 */
				Log.d("CardFactory", String.format(
						" > Skipping over an unmapped Field %s/%s (= %s)",
						model.cardClass.getName(), curField,
						curField.get(model)));
			}
		}

		return newCard;
	}
}
