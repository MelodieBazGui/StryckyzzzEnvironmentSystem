package math.absurd;

import bodies.Shape;
import math.Quat;
import math.Vec3;
import math.algorithm.EPA;
import math.algorithm.GJK;
import math.algorithm.EPA.PenetrationInfo;
import math.algorithm.GJK.Result;

public final class CollisionDetector {

    private CollisionDetector() {}

    public static EPA.PenetrationInfo detect(
            Shape A, Shape B,
            Quat qA, Vec3 pA,
            Quat qB, Vec3 pB
    ) {
        GJK.Result result = GJK.intersect(A, B, qA, pA, qB, pB);

        if (result == null || !result.intersect) {
            return null;
        }

        // Pass the simplex stored in the result to EPA
        return EPA.penetrationInfo(
                result.simplex,
                A, B,
                qA, pA,
                qB, pB
        );
    }
}
