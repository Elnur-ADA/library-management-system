package entities.enums;

public enum Genre {
    ROMANCE,
    FANTASY,
    SCIENCE_FICTION,
    MYSTERY,
    THRILLER,
    NON_FICTION,
    BIOGRAPHY,
    HISTORY,
    SELF_HELP,
    CHILDREN,
    YOUNG_ADULT,
    CLASSIC,
    HORROR,
    ADVENTURE,
    POETRY,
    COMICS,
    GRAPHIC_NOVEL,
    COOKBOOK,
    TRAVEL,
    RELIGION;


    public static Genre fromString(String genre) {
        try {
            return Genre.valueOf(genre.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid genre: " + genre);
        }
    }
}
