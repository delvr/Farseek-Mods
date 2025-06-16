package farseek.util

given [A1, A2, B1, B2] => (A1 Into B1, A2 Into B2)
   => (A1, A2) Into (B1, B2) = a => (a._1.into[B1], a._2.into[B2])

given [A1, A2, A3, B1, B2, B3] => (A1 Into B1, A2 Into B2, A3 Into B3)
   => (A1, A2, A3) Into (B1, B2, B3) = a => (a._1.into[B1], a._2.into[B2], a._3.into[B3])

given [S1, S2, E1, E2] => (S1 CanContain E1, S2 CanContain E2)
   => (S1, S2) CanContain (E1, E2) = (s, e) => (s._1 contains e._1) && (s._2 contains e._2)

given [S1, S2, S3, E1, E2, E3] => (S1 CanContain E1, S2 CanContain E2, S3 CanContain E3)
   => (S1, S2, S3) CanContain (E1, E2, E3) = (s, e) => (s._1 contains e._1) && (s._2 contains e._2) && (s._3 contains e._3)

given [S1, S2, T1, T2] => (S1 CanInclude T1, S2 CanInclude T2)
   => (S1, S2) CanInclude (T1, T2) = (s, t) => (s._1 includes t._1) && (s._2 includes t._2)

given [S1, S2, S3, T1, T2, T3] => (S1 CanInclude T1, S2 CanInclude T2, S3 CanInclude T3)
   => (S1, S2, S3) CanInclude (T1, T2, T3) = (s, t) => (s._1 includes t._1) && (s._2 includes t._2) && (s._3 includes t._3)

given [T1: EmptyTest, T2: EmptyTest]
   => EmptyTest[(T1, T2)] = x => x._1.isEmpty && x._2.isEmpty

given [T1: EmptyTest, T2: EmptyTest, T3: EmptyTest]
   => EmptyTest[(T1, T2, T3)] = x => x._1.isEmpty && x._2.isEmpty && x._3.isEmpty

given [T1: ZeroTest, T2: ZeroTest]
   => ZeroTest[(T1, T2)] = x => x._1.isZero && x._2.isZero

given [T1: ZeroTest, T2: ZeroTest, T3: ZeroTest]
   => ZeroTest[(T1, T2, T3)] = x => x._1.isZero && x._2.isZero && x._3.isZero

given [T1: Negation, T2: Negation]
   => Negation[(T1, T2)] = x => (-x._1, -x._2)

given [T1: Negation, T2: Negation, T3: Negation]
   => Negation[(T1, T2, T3)] = x => (-x._1, -x._2, -x._3)

given [T1: Addition, T2: Addition]
   => Addition[(T1, T2)] = (a, b) => (a._1 + b._1, a._2 + b._2)

given [T1: Addition, T2: Addition, T3: Addition]
   => Addition[(T1, T2, T3)] = (a, b) => (a._1 + b._1, a._2 + b._2, a._3 + b._3)

given [P1, P2, D1, D2] => (P1 ShiftedBy D1, P2 ShiftedBy D2)
   => (P1, P2) ShiftedBy (D1, D2) = (a, b) => (a._1 :+ b._1, a._2 :+ b._2)

given [P1, P2, P3, D1, D2, D3] => (P1 ShiftedBy D1, P2 ShiftedBy D2, P3 ShiftedBy D3)
   => (P1, P2, P3) ShiftedBy (D1, D2, D3) = (a, b) => (a._1 :+ b._1, a._2 :+ b._2, a._3 :+ b._3)

given [P1, P2, D1, D2] => (P1 SeparatedBy D1, P2 SeparatedBy D2)
   => (P1, P2) SeparatedBy (D1, D2) = (a, b) => (a._1 - b._1, a._2 - b._2)

given [P1, P2, P3, D1, D2, D3] => (P1 SeparatedBy D1, P2 SeparatedBy D2, P3 SeparatedBy D3)
   => (P1, P2, P3) SeparatedBy (D1, D2, D3) = (a, b) => (a._1 - b._1, a._2 - b._2, a._3 - b._3)

given [V1, V2, S] => (V1 ScaledBy S, V2 ScaledBy S)
   => (V1, V2) ScaledBy S = (v, s) => (v._1 :* s, v._2 :* s)

given [V1, V2, V3, S] => (V1 ScaledBy S, V2 ScaledBy S, V3 ScaledBy S)
   => (V1, V2, V3) ScaledBy S = (v, s) => (v._1 :* s, v._2 :* s, v._3 :* s)

given [P1: Interpolation, P2: Interpolation]
   => Interpolation[(P1, P2)] = (t, a, b) => (P1(t, a._1, b._1), P2(t, a._2, b._2))

given [P1: Interpolation, P2: Interpolation, P3: Interpolation]
   => Interpolation[(P1, P2, P3)] = (t, a, b) => (P1(t, a._1, b._1), P2(t, a._2, b._2), P3(t, a._3, b._3))
