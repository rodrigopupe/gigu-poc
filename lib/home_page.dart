import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const _control = MethodChannel('gigu_poc/control');
  static const _events = EventChannel('gigu_poc/events');

  final List<String> _log = [];
  bool _serviceEnabled = false;

  StreamSubscription<dynamic>? _subscription;

  @override
  void initState() {
    super.initState();
    _subscription = _events.receiveBroadcastStream().listen((event) {
      setState(() => _log.insert(0, event as String));
    });
    _refresh();
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  Future<void> _refresh() async {
    final enabled = await _control.invokeMethod<bool>('isServiceEnabled') ?? false;
    final saved = await _control.invokeMethod<List<dynamic>>('getLog') ?? [];

    debugPrint('refresh: enabled=$enabled saved=${saved.length}');

    if (!mounted) return;

    setState(() {
      _serviceEnabled = enabled;
      _log
        ..clear()
        ..addAll(saved.cast<String>().reversed);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('mini-GigU')),
      body: Column(
        children: [
          ListTile(
            title: Text(_serviceEnabled ? 'Serviço ativo' : 'Serviço inativo'),
            leading: Icon(
              Icons.circle,
              color: _serviceEnabled ? Colors.green : Colors.red,
            ),
          ),
          Wrap(
            spacing: 8,
            children: [
              ElevatedButton(
                onPressed: () => _control.invokeMethod('openAccessibilitySettings'),
                child: const Text('Acessibilidade'),
              ),
              ElevatedButton(
                onPressed: () => _control.invokeMethod('emitTest'),
                child: const Text('Emitir teste'),
              ),
              ElevatedButton(
                onPressed: () => _control.invokeMethod('emitDelayed'),
                child: const Text('Emitir em 8s'),
              ),
            ],
          ),
          const Divider(),
          Expanded(
            child: ListView.builder(
              itemCount: _log.length,
              itemBuilder: (_, i) => ListTile(
                dense: true,
                title: Text(_log[i]),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
