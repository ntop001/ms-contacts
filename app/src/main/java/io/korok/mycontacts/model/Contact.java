package io.korok.mycontacts.model;

/**
 * Contact represent a person or logical entity.
 */
public class Contact {
    public String firstName;
    public String lastName;
    public String title;
    public String avatar;
    public String introduction;

    public Contact(){
    }

    @Override
    public String toString() {
        return "Contact{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", title='" + title + '\'' +
                ", avatar='" + avatar + '\'' +
                ", introduction='" + introduction + '\'' +
                '}';
    }
}
