package org.openvasp.client.common;

import lombok.EqualsAndHashCode;

/**
 * The class Tuple2 follows the idea of the corresponding Scala class
 * https://www.scala-lang.org/api/2.12.x/scala/Tuple2.html
 *
 * @author Olexandr_Bilovol@epam.com
 */
@EqualsAndHashCode
public final class Tuple2<T1, T2> {

    public final T1 _1;
    public final T2 _2;

    private Tuple2(final T1 _1, final T2 _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public static <U1, U2> Tuple2<U1, U2> of(U1 _1, U2 _2) {
        return new Tuple2<>(_1, _2);
    }

    public T1 _1() {
        return _1;
    }

    public T2 _2() {
        return _2;
    }

    public T1 first() {
        return _1;
    }

    public T2 second() {
        return _2;
    }

    @Override
    public String toString() {
        return "(" + _1 + ", " + _2 + ")";
    }

}
