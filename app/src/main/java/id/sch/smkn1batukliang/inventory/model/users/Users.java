package id.sch.smkn1batukliang.inventory.model.users;

import android.os.Parcel;
import android.os.Parcelable;

public class Users implements Parcelable {

    public static final Creator<Users> CREATOR = new Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel in) {
            return new Users(in);
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };

    private String authId;
    private String email;
    private boolean emailVerification;
    private String employeeIdNumber;
    private String photoLink;
    private String level;
    private String position;
    private String timestamp;
    private String username;

    public Users() {
    }

    public Users(String authId, String email, boolean emailVerification, String employeeIdNumber, String photoLink, String level, String position, String timestamp, String username) {
        this.authId = authId;
        this.email = email;
        this.emailVerification = emailVerification;
        this.employeeIdNumber = employeeIdNumber;
        this.photoLink = photoLink;
        this.level = level;
        this.position = position;
        this.timestamp = timestamp;
        this.username = username;
    }

    protected Users(Parcel in) {
        authId = in.readString();
        email = in.readString();
        emailVerification = in.readByte() != 0;
        employeeIdNumber = in.readString();
        photoLink = in.readString();
        level = in.readString();
        position = in.readString();
        timestamp = in.readString();
        username = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authId);
        dest.writeString(email);
        dest.writeByte((byte) (emailVerification ? 1 : 0));
        dest.writeString(employeeIdNumber);
        dest.writeString(photoLink);
        dest.writeString(level);
        dest.writeString(position);
        dest.writeString(timestamp);
        dest.writeString(username);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerification() {
        return emailVerification;
    }

    public void setEmailVerification(boolean emailVerification) {
        this.emailVerification = emailVerification;
    }

    public String getEmployeeIdNumber() {
        return employeeIdNumber;
    }

    public void setEmployeeIdNumber(String employeeIdNumber) {
        this.employeeIdNumber = employeeIdNumber;
    }

    public String getPhotoLink() {
        return photoLink;
    }

    public void setPhotoLink(String photoLink) {
        this.photoLink = photoLink;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
