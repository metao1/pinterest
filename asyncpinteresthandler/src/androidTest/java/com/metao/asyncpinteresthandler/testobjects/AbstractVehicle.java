package com.metao.asyncpinteresthandler.testobjects;

public abstract class AbstractVehicle {
    protected String mName;
    protected int mWheels;

    public String getName() {
        return mName;
    }

    public int getWheels() {
        return mWheels;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AbstractVehicle) {
            if (mName.equals(((AbstractVehicle) o).getName())
                    && mWheels == ((AbstractVehicle) o).getWheels()) {
                return true;
            } else {
                return false;
            }
        } else {
            return super.equals(o);
        }
    }
}
