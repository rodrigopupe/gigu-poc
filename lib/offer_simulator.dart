import 'package:flutter/material.dart';

class Offer {
  final String price;
  final String pickup;
  final String trip;

  const Offer(this.price, this.pickup, this.trip);
}

const offers = [
  Offer('R\$ 18,50', 'Embarque: 4 min (1,2 km)', 'Viagem: 14 min (6,0 km)'),
  Offer('R\$ 32,00', 'Embarque: 3 min (1,0 km)', 'Viagem: 12 min (7,0 km)'),
  Offer('R\$ 20,25', 'Embarque: 7 min (3,0 km)', 'Viagem: 25 min (7,5 km)'),
  Offer('R\$ 1.250,00', 'Embarque: 10 min (4,5 km)', 'Viagem: 60 min (70,5 km)'),
];

class OfferSimulator extends StatefulWidget {
  const new({super.key});

  @override
  State<OfferSimulator> createState() => _OfferSimulatorState();
}

class _OfferSimulatorState extends State<OfferSimulator> {
  int _i = 0;

  @override
  Widget build(BuildContext context) {
    final offer = offers[_i];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Simulador de Oferta'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(offer.price, style: Theme.of(context).textTheme.displaySmall),
            const SizedBox(height: 16),
            Text(offer.pickup),
            Text(offer.trip),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () => setState(() => _i = (_i + 1) % offers.length),
              child: const Text('Próxima oferta'),
            ),
          ],
        ),
      ),
    );
  }
}
