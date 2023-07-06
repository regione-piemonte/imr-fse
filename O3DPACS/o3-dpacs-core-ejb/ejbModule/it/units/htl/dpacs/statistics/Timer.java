/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.statistics;

public class Timer {
		  /** Accumulatore contenente il numero dei millisecondi trascorsi. */
		  private long contatore;

		  /** Istante temporale dell'ultimo avvio del cronometro. */
		  private long avviato_a;

		  /** Variabile di stato che indica se il cronometro sta avanzando oppure no. */
		  private boolean avanzando;  
		  /**
		   * Costruttore: resetta il cronometro invocando il metodo d'istanza
		   * <code>azzera()</code>. Non avvia il conteggio; per fare cio' usare
		   * i metodi <code>avanza()</code> ed <code>avanzaDaCapo()</code>.
		   *
		   * @see   #reset()
		   * @see   #start()
		   * @see   #restart()
		   */
		  public Timer() { reset(); }

		  /** Metodo per (fermare ed) azzerare del cronometro. */
		  public void reset() {
		    synchronized (this) {
		      contatore = 0;
		      avanzando = false;
		    }
		  }

		  /**
		   * Metodo che fa (ri)partire il conteggio. Non azzera il
		   * cronometro, ma fa procedere la misura del tempo partendo dal
		   * valore immagazzinato nell'accumulatore.
		   * <p>
		   * Il cronometro puo' essere fermato mediante <code>stop()</code>.
		   *
		   * @see   #stop()
		   */
		  public void start() {
		    synchronized (this) {
		      avviato_a = System.currentTimeMillis();
		      avanzando = true;
		    }
		  }

		  /**
		   * Metodo che blocca l'avanzamento del cronometro. Usare
		   * <code>start()</code> per far ripartire il conteggio,
		   * <code>restart()</code> per azzerare il tutto prima di
		   * dare inizio al conteggio.
		   *
		   * @see   #start()
		   * @see   #restart()
		   */
		  public void stop() {
		    synchronized (this) {
		      contatore += System.currentTimeMillis() - avviato_a;
		      avanzando = false;
		    }
		  }

		  /** Azzera il cronometro e ne fa partire il conteggio. */
		  public void restart() {
		    reset();
		    start();
		  }

		  /**
		   * Lettura del conteggio corrente effettuato dal cronometro.
		   * Chiamate successive a questo metodo riportano valori diversi
		   * nel caso in cui il cronometro stia avanzando.
		   *
		   * @return   il numero totale di millisecondi contati dall'istanza.
		   */
		  public long getMeasure() {
		    synchronized (this) {
		      return avanzando ? contatore + System.currentTimeMillis() - avviato_a
		                       : contatore;
		    }
		  }
	  
		  /**
		   * Conversione in stringa del conteggio corrente. La lettura del
		   * valore viene effettuata mediante il metodo <code>leggi()</code>.
		   *
		   * @return   una stringa rappresentante il numero di millisecondi
		   *           contati dall'istanza in questione.
		   * @see      #getMeasure()
		   */
		  public String toString() {
		    return "" + getMeasure();
		  }
		}


