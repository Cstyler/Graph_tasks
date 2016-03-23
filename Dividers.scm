(define x (read))

(define memo '())

(define (dividers x)
  (if (null? memo)
      (let help ((i 1) (result '()) (s (inexact->exact (floor (sqrt x)))))
        (let ((r (remainder x i)))
          (if (> i s)
              (let ((r (cdr (reverse (map (lambda (t) (/ x t)) result)))))
                (set! memo (append r (if (= (car result) s) (cdr result) result)))
                r)
              (if (= r 0)
                  (help (+ i 1) (cons i result) s)
                  (help (+ i 1) result s)))))
      (let ((m1 (member x memo)))
        (if m1
            (let help ((result '()) (m (cdr m1)))
              (format #t "~a ~a ~a\n" (car m) (inexact->exact (floor (sqrt x))) x)
              (if (or (null? m) (not m))
                  (reverse result)
                  (let ((r (remainder x (car m))))
                    (if (= r 0)
                        (if (<= (car m) (inexact->exact (floor (sqrt x))))
                            (reverse (cons (car m) result))
                            (help (cons (car m) result) (cdr m)))
                        (help result (cdr m))))))
            '()))))

(define (divides-rest? lst x)
  (define (help lst)
    (if (null? lst)
        #f
        (if (= (remainder (car lst) x) 0)
            #t
            (divides-rest? (cdr lst) x))))
  (help lst))

(define (search-div divisors)
  (let help ((vertices '()) (ds divisors))
    (if (null? ds)
        vertices
        (if (divides-rest? vertices (car ds))
            (help vertices (cdr ds))
            (help (cons (car ds) vertices) (cdr ds))))))

(define vertices '())

(define (graph-search x)
  (let* ((current-vertices (search-div (dividers x))) (vertex `(,x ,current-vertices)))
    (if (not (member vertex vertices))
        (begin (set! vertices (cons vertex vertices))
               (for-each (lambda (x) (graph-search x)) current-vertices)))))

(define (dot-graph g)
  (graph-search x)
  
  (format #t "graph {\n\t~a\n" x)
  
  (let v-write ((lst memo))
    (if (not (null? lst))
        (begin
          (format #t "\t~a\n" (car lst))
          (v-write (cdr lst)))))
  
  (let edge-write ((lst vertices))
    (if (not (null? lst))
        (begin
          (for-each (lambda (x) (format #t "\t~a -- ~a\n" (caar lst) x)) (cadar lst))
          (edge-write (cdr lst)))))
  
  (display "}\n"))

(dot-graph vertices)
