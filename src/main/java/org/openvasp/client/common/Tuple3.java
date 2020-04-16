package org.openvasp.client.common;

import lombok.EqualsAndHashCode;

/**
 * The class Tuple3 follows the idea of the corresponding Scala class
 * https://www.scala-lang.org/api/2.12.x/scala/Tuple3.html
 *
 * @author Olexandr_Bilovol@epam.com
 */
@EqualsAndHashCode
public final class Tuple3<T1, T2, T3> {

    public final T1 _1;
    public final T2 _2;
    public final T3 _3;

    private Tuple3(final T1 _1, final T2 _2, final T3 _3) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
    }

    public static <U1, U2, U3> Tuple3<U1, U2, U3> of(U1 _1, U2 _2, U3 _3) {
        return new Tuple3<>(_1, _2, _3);
    }

    public T1 _1() {
        return _1;
    }

    public T2 _2() {
        return _2;
    }

    public T3 _3() {
        return _3;
    }

    @Override
    public String toString() {
        return "(" + _1 + ", " + _2 + ", " + _3 + ")";
    }

}


