package mwg.wb.test;

public class User {
//	User(String aName,int aid ) {
//		Name=aName;
//		id=aid;
//	}
	
	int age; int name;

    public User(  int n, int m) {
    	name=n;
    	age=m;
	}

	public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }
}
