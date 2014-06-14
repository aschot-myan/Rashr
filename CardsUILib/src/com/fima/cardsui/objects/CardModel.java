package com.fima.cardsui.objects;

import java.io.Serializable;

/**
 * A card model that represents all the basic information about a {@link Card}
 * (actually, it's a concrete copy of {@link AbstractCard} that's
 * {@link Serializable}). CardModel objects can be used to store information
 * about cards across configuration changes, or even saved to USB storage.
 * 
 * <p>
 * To turn a {@link CardModel} into a Card, use {@link CardFactory}.
 * 
 * @author FLamparski
 * @see {@link AbstractCard}, {@link CardFactory}
 */
public class CardModel implements Serializable {
	private static final long serialVersionUID = 0xDEADBEEFl;
	protected int image;
	protected String description, color, titleColor, desc, title, titlePlay;
	protected Boolean hasOverflow, isClickable;
	protected int imageRes;

	protected Class<? extends AbstractCard> cardClass;
	protected Object data;

	/**
	 * A very minimal and customizable constructor
	 * 
	 * @param cardClass
	 */
	public CardModel(Class<? extends AbstractCard> cardClass) {
		this.cardClass = cardClass;
	}

	/**
	 * For basic cards
	 * 
	 * @param description
	 * @param title
	 */
	public CardModel(String description, String title,
			Class<? extends AbstractCard> cardClass) {
		this.description = description;
		this.desc = description;
		this.title = title;
		this.cardClass = cardClass;
	}

	/**
	 * For basic cards w/ data
	 * 
	 * @param description
	 * @param title
	 * @param data
	 */
	public CardModel(String description, String title, Object data,
			Class<? extends AbstractCard> cardClass) {
		this.description = description;
		this.desc = description;
		this.title = title;
		this.data = data;
		this.cardClass = cardClass;
	}

	/**
	 * For Play cards
	 * 
	 * @param titlePlay
	 * @param description
	 * @param color
	 * @param titleColor
	 * @param hasOverflow
	 * @param isClickable
	 */
	public CardModel(String titlePlay, String description, String color,
			String titleColor, Boolean hasOverflow, Boolean isClickable,
			Class<? extends AbstractCard> cardClass) {
		this.titlePlay = titlePlay;
		this.description = description;
		this.color = color;
		this.titleColor = titleColor;
		this.hasOverflow = hasOverflow;
		this.isClickable = isClickable;
		this.cardClass = cardClass;
	}

	/**
	 * For Play cards w/ data
	 * 
	 * @param titlePlay
	 * @param description
	 * @param color
	 * @param titleColor
	 * @param hasOverflow
	 * @param isClickable
	 * @param data
	 */
	public CardModel(String titlePlay, String description, String color,
			String titleColor, Boolean hasOverflow, Boolean isClickable,
			Object data, Class<? extends AbstractCard> cardClass) {
		this.titlePlay = titlePlay;
		this.description = description;
		this.color = color;
		this.titleColor = titleColor;
		this.hasOverflow = hasOverflow;
		this.isClickable = isClickable;
		this.data = data;
		this.cardClass = cardClass;
	}

	/**
	 * The full monty constructor.
	 * 
	 * @param image
	 * @param description
	 * @param color
	 * @param titleColor
	 * @param desc
	 * @param title
	 * @param titlePlay
	 * @param hasOverflow
	 * @param isClickable
	 * @param imageRes
	 * @param cardClass
	 * @param data
	 */
	public CardModel(int image, String description, String color,
			String titleColor, String desc, String title, String titlePlay,
			Boolean hasOverflow, Boolean isClickable, int imageRes,
			Class<? extends AbstractCard> cardClass, Object data) {
		this.image = image;
		this.description = description;
		this.color = color;
		this.titleColor = titleColor;
		this.desc = desc;
		this.title = title;
		this.titlePlay = titlePlay;
		this.hasOverflow = hasOverflow;
		this.isClickable = isClickable;
		this.imageRes = imageRes;
		this.cardClass = cardClass;
		this.data = data;
	}

	public String getColor() {
		return color;
	}

	/**
	 * @return Arbitrary data associated with this card model
	 */
	public Object getData() {
		return data;
	}

	public String getDesc() {
		return desc;
	}

	public String getDescription() {
		return description;
	}

	public Boolean getHasOverflow() {
		return hasOverflow;
	}

	public int getImage() {
		return image;
	}

	public int getImageRes() {
		return imageRes;
	}

	public Boolean getIsClickable() {
		return isClickable;
	}

	public String getTitle() {
		return title;
	}

	public String getTitleColor() {
		return titleColor;
	}

	public String getTitlePlay() {
		return titlePlay;
	}

	/**
	 * @return the card's type
	 */
	public Class<? extends AbstractCard> getType() {
		return cardClass;
	}

	/**
	 * @param color
	 *            the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * @param Arbitrary
	 *            data associated with this card model (POJOs plx, no context
	 *            leaks)
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * @param desc
	 *            the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param hasOverflow
	 *            the hasOverflow to set
	 */
	public void setHasOverflow(Boolean hasOverflow) {
		this.hasOverflow = hasOverflow;
	}

	/**
	 * @param image
	 *            the image to set
	 */
	public void setImage(int image) {
		this.image = image;
	}

	/**
	 * @param imageRes
	 *            the imageRes to set
	 */
	public void setImageRes(int imageRes) {
		this.imageRes = imageRes;
	}

	/**
	 * @param isClickable
	 *            the isClickable to set
	 */
	public void setIsClickable(Boolean isClickable) {
		this.isClickable = isClickable;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param titleColor
	 *            the titleColor to set
	 */
	public void setTitleColor(String titleColor) {
		this.titleColor = titleColor;
	}

	/**
	 * @param titlePlay
	 *            the titlePlay to set
	 */
	public void setTitlePlay(String titlePlay) {
		this.titlePlay = titlePlay;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Class<? extends AbstractCard> type) {
		this.cardClass = type;
	}
}
